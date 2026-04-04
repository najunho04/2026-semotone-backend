package com.semotone.semotone.domain.ai.repository;

import com.google.cloud.firestore.*;
import com.semotone.semotone.domain.ai.entity.AiResultEntity;
import com.semotone.semotone.domain.post.dto.PostResDto;
import com.semotone.semotone.domain.post.entity.PostEntity;
import com.semotone.semotone.domain.user.entity.UserEntity;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Slf4j
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
    //o
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

    @Override
    public List<AiResultEntity> findAllAiResult(){
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME).get().get();
            List<AiResultEntity> result = new ArrayList<>();
            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                result.add(doc.toObject(AiResultEntity.class));
            }
            log.info("Firestore: all users fetched [count: {}]", result.size());
            return result;
        } catch (Exception e) {
            log.error("Firestore all users fetch failed", e);
            throw new RuntimeException("DB 조회 중 오류가 발생했습니다.");
        }
    }
}
