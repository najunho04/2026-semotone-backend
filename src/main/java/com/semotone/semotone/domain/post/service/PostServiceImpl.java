package com.semotone.semotone.domain.post.service;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.semotone.semotone.domain.ai.dto.AiReqDto;
import com.semotone.semotone.domain.ai.dto.AiResDto;
import com.semotone.semotone.domain.ai.service.AiService;
import com.semotone.semotone.domain.post.dto.PostCreateReqDto;
import com.semotone.semotone.domain.post.dto.PostResDto;
import com.semotone.semotone.domain.post.entity.PostEntity;
import com.semotone.semotone.domain.post.repository.PostRepository;
import com.semotone.semotone.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final UserService userService;
    private final AiService aiService;

    @Override
    public String createPost(PostCreateReqDto reqDto) throws ExecutionException, InterruptedException {
        validateRewardPoint(reqDto.getRewardPoint());
        userService.usePoints(reqDto.getUserId(), reqDto.getRewardPoint());

        PostEntity post = PostEntity.builder()
                .userId(reqDto.getUserId())
                .title(reqDto.getTitle())
                .content(reqDto.getContent())
                .rewardPoint(reqDto.getRewardPoint())
                .latitude(reqDto.getLatitude())
                .longitude(reqDto.getLongitude())
                .isCreated(Timestamp.now())
                .deleted(false)
                .accepted(false)
                .completed(false)
                .accepted_userId(null)
                .build();

        String postId = postRepository.save(post);

        try {
            userService.addPostIdToUser(reqDto.getUserId(), postId);

            AiReqDto aiReqDto = AiReqDto.builder().text(reqDto.getContent()).build();
            AiResDto aiResDto = aiService.analyzePostText(aiReqDto);
            aiService.saveAiResult(postId, aiResDto);

            return postId;
        } catch (Exception e) {
            try {
                postRepository.delete(postId);
                userService.removePostIdFromUser(reqDto.getUserId(), postId);
                userService.increasePointAndAcceptCount(reqDto.getUserId(), reqDto.getRewardPoint(), 0);
            } catch (Exception rollbackException) {
                throw new RuntimeException(
                        "게시글 생성 중 롤백까지 실패했습니다. postId=" + postId +
                                " [원인: " + e.getMessage() + "] [롤백 실패: " + rollbackException.getMessage() + "]",
                        e
                );
            }

            throw new RuntimeException("게시글 생성 중 실패하여 변경사항을 롤백했습니다. " + e.getMessage(), e);
        }
    }

    @Override
    public void acceptPost(String postId, String acceptingUserId) throws ExecutionException, InterruptedException {
        boolean accepted = postRepository.tryAcceptPost(postId, acceptingUserId);
        if (!accepted) {
            throw new RuntimeException("이미 수락된 게시글입니다.");
        }
    }

    @Override
    public void completePost(String postId, String requesterUserId) throws ExecutionException, InterruptedException {
        PostEntity post = postRepository.findById(postId);
        if (post == null) {
            throw new RuntimeException("게시글을 찾을 수 없습니다.");
        }

        if (post.getAccepted_userId() == null || post.getAccepted_userId().isBlank()) {
            throw new RuntimeException("수락된 사용자가 없어 완료 처리할 수 없습니다.");
        }

        int rewardPoint = postRepository.completePost(postId, requesterUserId);
        userService.increasePointAndAcceptCount(post.getAccepted_userId(), rewardPoint, 1);
    }

    @Override
    public PostResDto getPostDetail(String postId) throws ExecutionException, InterruptedException {
        PostEntity entity = postRepository.findById(postId);
        if (entity == null) {
            throw new RuntimeException("게시글을 찾을 수 없습니다.");
        }
        return PostResDto.fromEntity(postId, entity);
    }

    @Override
    public List<PostResDto> getPostList(double userLat, double userLng, String sortBy) throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documents = postRepository.findAllUnaccepted();
        List<PostResDto> postList = new ArrayList<>();

        for (QueryDocumentSnapshot doc : documents) {
            PostEntity entity = doc.toObject(PostEntity.class);
            postList.add(PostResDto.fromEntity(doc.getId(), entity));
        }

        if ("latest".equalsIgnoreCase(sortBy)) {
            postList.sort((p1, p2) -> Long.compare(p2.getCreatedAt(), p1.getCreatedAt()));
        } else {
            postList.sort((p1, p2) -> {
                double dist1 = calculateDistance(userLat, userLng, p1.getLatitude(), p1.getLongitude());
                double dist2 = calculateDistance(userLat, userLng, p2.getLatitude(), p2.getLongitude());
                return Double.compare(dist1, dist2);
            });
        }

        return postList;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int earthRadiusKm = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c * 1000;
    }

    private void validateRewardPoint(int rewardPoint) {
        if (rewardPoint <= 0) {
            throw new RuntimeException("지급 포인트는 1 이상이어야 합니다.");
        }
    }
}
