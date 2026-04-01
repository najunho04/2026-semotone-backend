package com.semotone.semotone.domain.post.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.semotone.semotone.domain.post.entity.PostEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
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

            if (!snapshot.exists()) {
                throw new RuntimeException("게시글을 찾을 수 없습니다.");
            }

            // 이미 수락된 게시글이면 false 반환
            Boolean isAccept = snapshot.getBoolean("accepted");
            if (Boolean.TRUE.equals(isAccept)) {
                return false;
            }

            String writerUserId = snapshot.getString("userId");
            if (writerUserId != null && writerUserId.equals(acceptingUserId)) {
                throw new RuntimeException("작성자는 본인 게시글을 수락할 수 없습니다.");
            }

            // 수락 처리 (트랜잭션 내에서 원자적으로 업데이트)
            transaction.update(docRef, "accepted", true, "accepted_userId", acceptingUserId);
            return true;
        }).get();
    }

    @Override
    public int completePost(String postId, String requesterUserId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(collection_name).document(postId);

        return firestore.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(docRef).get();

            if (!snapshot.exists()) {
                throw new RuntimeException("게시글을 찾을 수 없습니다.");
            }

            String writerUserId = snapshot.getString("userId");
            if (writerUserId == null || !writerUserId.equals(requesterUserId)) {
                throw new RuntimeException("게시글 작성자만 완료 처리할 수 있습니다.");
            }

            Boolean accepted = snapshot.getBoolean("accepted");
            if (!Boolean.TRUE.equals(accepted)) {
                throw new RuntimeException("수락된 사용자가 없어 완료 처리할 수 없습니다.");
            }

            Boolean completed = snapshot.getBoolean("completed");
            if (Boolean.TRUE.equals(completed)) {
                throw new RuntimeException("이미 완료된 게시글입니다.");
            }

            Long rewardPoint = snapshot.getLong("rewardPoint");
            transaction.update(docRef, "completed", true, "deleted", true);
            return rewardPoint == null ? 0 : rewardPoint.intValue();
        }).get();
    }

    //특정 id로 게시글 상세 조회
    @Override
    public PostEntity findById(String postId) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = firestore.collection(collection_name).document(postId).get().get();
        if (document.exists()) {
            return document.toObject(PostEntity.class); // DB 데이터를 Entity로 변환해서 반환
        }
        return null; // 데이터 없으면 null 반환
    }

    //수락이 아직 안된, 삭제가 아직 안된 게시글 목록 조회
    @Override
    public List<QueryDocumentSnapshot> findAllUnaccepted() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(collection_name)
                .whereEqualTo("accepted", false)// 수락 안 된 것만
                .whereEqualTo("deleted", false)// 삭제 안 된 것만
                .get();
        return future.get().getDocuments();
    }

    // 게시글 삭제 (AI 분석 실패 시 롤백용)
    @Override
    public void delete(String postId) throws ExecutionException, InterruptedException {
        firestore.collection(collection_name).document(postId).delete().get();
    }
}
