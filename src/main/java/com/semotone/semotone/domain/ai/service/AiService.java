package com.semotone.semotone.domain.ai.service;

import com.semotone.semotone.domain.ai.dto.AiReqDto;
import com.semotone.semotone.domain.ai.dto.AiResDto;
import com.semotone.semotone.domain.ai.dto.AiResultResDto;

public interface AiService {
    /**
     * 1. 제미나이 API 호출 및 텍스트 분석 (분석 전담)
     * - 게시글 본문(AiReqDto)을 받아 제미나이 API를 호출합니다.
     * - 제미나이의 응답을 파싱하여 AiResDto(카테고리, 물건, 출발/도착지 등)로 반환합니다.
     * - DB 접근은 전혀 하지 않고 오직 외부 API 통신만 담당합니다.
     */
    AiResDto analyzePostText(AiReqDto aiReqDto);

    /**
     * 2. AI 분석 결과 DB 저장 (저장 전담)
     * - 분석이 완료된 결과(AiResDto)와 게시글 ID(postId)를 받아 Firestore에 저장합니다.
     * - 저장 후, 클라이언트에게 응답하기 좋은 형태인 AiResultResDto로 변환하여 반환합니다.
     */
    AiResultResDto saveAiResult(String postId, AiResDto aiResDto);

    /**
     * 3. 게시글 ID로 AI 분석 결과 단건 조회 (조회 전담)
     * - 특정 게시물에 매핑된 AI 태그 정보를 DB에서 꺼내옵니다.
     */
    AiResultResDto getAiResult(String postId);
}
