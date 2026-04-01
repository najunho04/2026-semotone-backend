package com.semotone.semotone.domain.ai.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiReqDto {
    // 제미나이에게 분석을 맡길 게시글의 본문 내용
    // 예: "중도 벗터에 책 놓고 왔는데 전정대로 가져다주실 분"
    private String text;
}