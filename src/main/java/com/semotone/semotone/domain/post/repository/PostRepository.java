package com.semotone.semotone.domain.post.repository;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.semotone.semotone.domain.post.entity.PostEntity;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface PostRepository {

    /**
     * 게시글 저장
     * @return postId
     */
    String save(PostEntity post) throws ExecutionException, InterruptedException;

    /**
     * 게시글 수락 처리 - Firestore 트랜잭션으로 isAccept 확인 및 업데이트 (동시성 보장)
     * @return true: 수락 성공 / false: 이미 수락된 게시글
     */
    boolean tryAcceptPost(String postId, String acceptingUserId) throws ExecutionException, InterruptedException;

    /**
     * 게시글 조회
     * @return PostEntity
     */
    PostEntity findById(String postId) throws ExecutionException, InterruptedException;

    /**
     * 게시글 전체 조회
     * @return List<QueryDocumentSnapshot> -> List<PostResDto> 으로 서비스로직에서 return 예정
     */
    List<QueryDocumentSnapshot> findAllUnaccepted() throws ExecutionException, InterruptedException;

    /**
     * 게시글 삭제 (롤백용)
     * - AI 분석 실패 시 저장된 게시글을 삭제하기 위해 사용
     */
    void delete(String postId) throws ExecutionException, InterruptedException;

}
