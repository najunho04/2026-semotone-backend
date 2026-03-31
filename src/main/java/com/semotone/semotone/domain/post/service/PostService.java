package com.semotone.semotone.domain.post.service;

import com.semotone.semotone.domain.post.dto.PostCreateReqDto;

import java.util.concurrent.ExecutionException;

public interface PostService {
    String createPost(PostCreateReqDto reqDto) throws ExecutionException, InterruptedException;

    /**
     * 게시글 수락 처리 - 수락한 유저의 포인트와 acceptCount 증가
     */
    void acceptPost(String postId, String acceptingUserId) throws ExecutionException, InterruptedException;
}
