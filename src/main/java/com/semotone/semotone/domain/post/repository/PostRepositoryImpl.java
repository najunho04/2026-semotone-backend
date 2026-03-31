package com.semotone.semotone.domain.post.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.semotone.semotone.domain.post.entity.PostEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepository {

    public static final String collection_name = "posts";
    private final Firestore firestore;

    @Override
    public String save(PostEntity post) throws ExecutionException, InterruptedException {
        ApiFuture<DocumentReference> apiFuture = firestore.collection(collection_name).add(post);
        return apiFuture.get().getId();
    }

    @Override
    public boolean tryAcceptPost(String postId, String acceptingUserId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(collection_name).document(postId);

        // 트랜잭션으로 읽기 + 쓰기를 원자적으로 처리 → 동시성 완벽 방어
        return firestore.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(docRef).get();

            // 이미 수락된 게시글이면 false 반환
            Boolean isAccept = snapshot.getBoolean("isAccept");
            if (Boolean.TRUE.equals(isAccept)) {
                return false;
            }

            // 수락 처리 (트랜잭션 내에서 원자적으로 업데이트)
            transaction.update(docRef, "isAccept", true, "accepted_userId", acceptingUserId);
            return true;
        }).get();
    }
}
