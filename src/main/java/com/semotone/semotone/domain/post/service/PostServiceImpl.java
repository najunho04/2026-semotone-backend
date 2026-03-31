package com.semotone.semotone.domain.post.service;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.GeoPoint;
import com.google.cloud.firestore.QueryDocumentSnapshot;
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

    @Override
    public String createPost(PostCreateReqDto reqDto) throws ExecutionException, InterruptedException {

        PostEntity post = PostEntity.builder()
                .userId(reqDto.getUserId())
                .title(reqDto.getTitle())
                .content(reqDto.getContent())
                .latitude(reqDto.getLatitude())
                .longitude(reqDto.getLongitude())
                .isCreated(Timestamp.now()) // 현재 시간을 작성 시간으로 세팅
                .isDelete(false)            // 처음 생성 시 삭제 여부는 false
                .isAccept(false)            // 처음 생성 시 수락 여부는 false
                .accepted_userId(null)       // 수락자가 없으므로 null
                .build();

        // 게시글 저장 후 반환된 ID를 사용
        String postId = postRepository.save(post);

        // 유저의 myPosts 리스트에 postId 추가
        userService.addPostIdToUser(reqDto.getUserId(), postId);

        return postId;
    }

    @Override
    public void acceptPost(String postId, String acceptingUserId) throws ExecutionException, InterruptedException {
        // 1. 트랜잭션으로 이미 수락 여부 확인 + 게시글 상태 업데이트 (동시성 보장)
        boolean accepted = postRepository.tryAcceptPost(postId, acceptingUserId);

        if (!accepted) {
            throw new RuntimeException("이미 수락된 게시글입니다.");
        }

        // 2. 수락 성공 시에만 유저 포인트와 acceptCount 증가
        userService.increasePointAndAcceptCount(acceptingUserId, 10, 1);
    }

    // 게시글 상세 조회
    @Override
    public PostResDto getPostDetail(String postId) throws ExecutionException, InterruptedException {
        PostEntity entity = postRepository.findById(postId);
        if (entity == null) {
            throw new RuntimeException("게시글을 찾을 수 없습니다.");
        }
        return PostResDto.fromEntity(postId, entity);
    }

    // 게시글 목록 조회 (거리순, 최신순 정렬)
    @Override
    public List<PostResDto> getPostList(double userLat, double userLng, String sortBy) throws ExecutionException, InterruptedException {
        // 1. Repository에서 수락/삭제 안 된 목록 다 가져오기
        List<QueryDocumentSnapshot> documents = postRepository.findAllUnaccepted();
        List<PostResDto> postList = new ArrayList<>();

        // 2. 가져온 데이터를 DTO로 변환해서 리스트에 담기
        for (QueryDocumentSnapshot doc : documents) {
            PostEntity entity = doc.toObject(PostEntity.class);
            postList.add(PostResDto.fromEntity(doc.getId(), entity));
        }

        // 3. 정렬 기준(sortBy)에 따른 정렬
        if ("latest".equalsIgnoreCase(sortBy)) {
            // 최신순 정렬 (작성 시간이 큰 것(최신)이 위로 오도록 내림차순)
            postList.sort((p1, p2) -> Long.compare(p2.getCreatedAt(), p1.getCreatedAt()));
        } else {
            // 거리순 정렬 (기본값, 나와 가까운 순서대로 오름차순)
            postList.sort((p1, p2) -> {
                double dist1 = calculateDistance(userLat, userLng, p1.getLatitude(), p1.getLongitude());
                double dist2 = calculateDistance(userLat, userLng, p2.getLatitude(), p2.getLongitude());
                return Double.compare(dist1, dist2);
            });
        }

        return postList;
    }

    // 두 위도경도 사이의 거리를 계산하는 공식 (미터 단위 반환)
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 지구의 반지름 (km)
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000;
    }

}
