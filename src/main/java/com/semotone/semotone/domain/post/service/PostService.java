package com.semotone.semotone.domain.post.service;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.GeoPoint;
import com.semotone.semotone.domain.post.dto.PostCreateReqDto;
import com.semotone.semotone.domain.post.entity.PostEntity;
import com.semotone.semotone.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor

public class PostService {
    private final PostRepository postRepository;
    public String createPost(PostCreateReqDto reqDto) throws ExecutionException, InterruptedException {

        PostEntity post = PostEntity.builder()
                .userId(reqDto.getUserId())
                .title(reqDto.getTitle())
                .content(reqDto.getContent())
                .location(new GeoPoint(reqDto.getLatitude(), reqDto.getLongitude()))
                .isCreated(Timestamp.now()) // 현재 시간을 작성 시간으로 세팅
                .isDelete(false)            // 처음 생성 시 삭제 여부는 false
                .isAccept(false)            // 처음 생성 시 수락 여부는 false
                .accepted_userId(null)       // 수락자가 없으므로 null
                .build();

        return postRepository.save(post);
    }

}
