package com.semotone.semotone.domain.post.service;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.GeoPoint;
import com.semotone.semotone.domain.post.dto.PostCreateReqDto;
import com.semotone.semotone.domain.post.entity.PostEntity;
import com.semotone.semotone.domain.post.repository.PostRepository;
import com.semotone.semotone.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

}
