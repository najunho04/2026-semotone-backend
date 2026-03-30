package com.semotone.semotone.domain.post.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.semotone.semotone.domain.post.entity.PostEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class PostRepository {

    public static final String collection_name = "posts";
    private final Firestore firestore;
    public String save(PostEntity post) throws ExecutionException, InterruptedException {
        ApiFuture<DocumentReference> apiFuture = firestore.collection(collection_name).add(post);
        return apiFuture.get().getId();
    }
}
