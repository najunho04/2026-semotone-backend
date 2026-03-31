package com.semotone.semotone.domain.post.service;

import com.semotone.semotone.domain.post.dto.PostCreateReqDto;
import com.semotone.semotone.domain.post.dto.PostResDto;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface PostService {

    /**
     * 게시글 작성
     */
    String createPost(PostCreateReqDto reqDto) throws ExecutionException, InterruptedException;

    /**
     * 게시글 수락 처리 - 수락한 유저의 포인트와 acceptCount 증가
     */
    void acceptPost(String postId, String acceptingUserId) throws ExecutionException, InterruptedException;

    /**
     * 게시글 상세 조회
     */
    PostResDto getPostDetail(String postId) throws ExecutionException, InterruptedException;

    /**
     * 전체 게시물 조회
     */
    List<PostResDto> getPostList(double userLat, double userLng, String sortBy) throws ExecutionException, InterruptedException;

}
