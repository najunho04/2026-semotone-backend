package com.semotone.semotone.domain.post.service;

import com.semotone.semotone.domain.post.dto.PostCreateReqDto;
import com.semotone.semotone.domain.post.dto.PostResDto;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface PostService {

    String createPost(PostCreateReqDto reqDto) throws ExecutionException, InterruptedException;

    void acceptPost(String postId, String acceptingUserId) throws ExecutionException, InterruptedException;

    void completePost(String postId, String requesterUserId) throws ExecutionException, InterruptedException;

    PostResDto getPostDetail(String postId) throws ExecutionException, InterruptedException;

    List<PostResDto> getPostList(double userLat, double userLng, String sortBy) throws ExecutionException, InterruptedException;
}
