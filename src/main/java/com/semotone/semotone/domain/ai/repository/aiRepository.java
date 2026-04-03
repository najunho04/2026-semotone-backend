package com.semotone.semotone.domain.ai.repository;

import com.semotone.semotone.domain.ai.dto.AiReqDto;
import com.semotone.semotone.domain.ai.dto.AiResDto;
import com.semotone.semotone.domain.ai.entity.AiResultEntity;

import java.util.List;
import java.util.Optional;

public interface aiRepository {

    /**
     * 1. AI 분석 결과 저장 (생성 및 수정)
     * - 제미나이 API로부터 응답(AiResDto)을 받은 후, 이를 Entity로 변환하여 Firestore에 저장합니다.
     * - postId를 문서 ID(Document ID)로 사용하므로, 수정할 때도 이 메서드를 그대로 쓰면 덮어쓰기가 됩니다.
     */
    void saveAiReq(AiResultEntity aiResult);

    /**
     * 2. 게시글 ID(postId)로 AI 분석 결과 단건 조회
     * - 클라이언트가 게시글 상세 보기를 요청할 때, Post 데이터와 함께 AI 태그 데이터를 내려주기 위해 사용합니다.
     * - AI 분석이 실패했거나 아직 완료되지 않아서 데이터가 없을 수도 있으므로 Optional로 감싸줍니다.
     */
    Optional<AiResultEntity> findByPostId(String postId);

    /**
     * 3. 게시글 매핑되는 ai_result 전체 조회
     * - 태그, 응급도에 따른 필터링 기능 구현에 사용됩니다.
     *
     */
    List<AiResultEntity> findAllAiResult();
}
