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
public class PostRepository {

    public static final String collection_name = "posts";
    private final Firestore firestore;
    //게시글 저장
    public String save(PostEntity post) throws ExecutionException, InterruptedException {
        ApiFuture<DocumentReference> apiFuture = firestore.collection(collection_name).add(post);
        return apiFuture.get().getId();
    }
    //특정 id로 게시글 상세 조회
    public PostEntity findById(String postId) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = firestore.collection(collection_name).document(postId).get().get();
        if (document.exists()) {
            return document.toObject(PostEntity.class); // DB 데이터를 Entity로 변환해서 반환
        }
        return null; // 데이터 없으면 null 반환
    }
    //수락이 아직 안된, 삭제가 아직 안된 게시글 목록 조회
    public List<QueryDocumentSnapshot> findAllUnaccepted() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(collection_name)
                .whereEqualTo("isAccept", false) // 수락 안 된 것만
                .whereEqualTo("isDelete", false) // 삭제 안 된 것만
                .get();
        return future.get().getDocuments();
    }
}
