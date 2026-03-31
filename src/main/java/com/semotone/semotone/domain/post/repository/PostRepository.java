package com.semotone.semotone.domain.post.repository;

import com.semotone.semotone.domain.post.entity.PostEntity;

import java.util.concurrent.ExecutionException;

public interface PostRepository {
    String save(PostEntity post) throws ExecutionException, InterruptedException;

    /**
     * 게시글 수락 처리 - Firestore 트랜잭션으로 isAccept 확인 및 업데이트 (동시성 보장)
     * @return true: 수락 성공 / false: 이미 수락된 게시글
     */
    boolean tryAcceptPost(String postId, String acceptingUserId) throws ExecutionException, InterruptedException;
}
