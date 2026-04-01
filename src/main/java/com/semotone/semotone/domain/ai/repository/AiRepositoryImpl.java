package com.semotone.semotone.domain.ai.repository;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.semotone.semotone.domain.ai.entity.AiResultEntity;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class AiRepositoryImpl implements aiRepository {

    public static final String COLLECTION_NAME = "ai_results";
    private final Firestore firestore;

    public AiRepositoryImpl(Firestore firestore) {
        this.firestore = firestore;
    }

    /**
     * AI 분석 결과를 Firestore에 저장
     * postId를 문서 ID로 사용하므로, 동일 postId로 재저장 시 덮어쓰기됨
     */
    @Override
    public void saveAiReq(AiResultEntity aiResult) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(aiResult.getPostId());
            docRef.set(aiResult).get(); // .get()으로 동기 처리
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("AI 분석 결과 저장 실패: " + e.getMessage(), e);
        }
    }

    /**
     * postId로 AI 분석 결과 조회
     * AI 분석이 아직 완료되지 않았거나 실패한 경우 Optional.empty() 반환
     */
    @Override
    public Optional<AiResultEntity> findByPostId(String postId) {
        try {
            DocumentSnapshot document = firestore.collection(COLLECTION_NAME).document(postId).get().get();
            if (document.exists()) {
                return Optional.ofNullable(document.toObject(AiResultEntity.class));
            }
            return Optional.empty();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("AI 분석 결과 조회 실패: " + e.getMessage(), e);
        }
    }
}
