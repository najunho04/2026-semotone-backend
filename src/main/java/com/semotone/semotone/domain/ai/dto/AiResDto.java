package com.semotone.semotone.domain.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter         // Jackson 역직렬화 시 필드 세팅에 필요
@NoArgsConstructor // JSON 역직렬화를 위해 필수
@AllArgsConstructor
@Builder
public class AiResDto {
    private String category; // 예: "배달"
    private String object;   // 예: "책"
    private String type;     // 예: "물건전달"
    private String urgency;  // 예: "보통"

    // AI가 텍스트에서 추출한 장소 정보 (선택적 활용)
    private String fromLocation; // 예: "library" (중도)
    private String toLocation;   // 예: "elecNinfo" (전정대)

    private TagsDto tags; // 계층 구조 유지


    @Getter
    @Setter            // Jackson 역직렬화 시 필드 세팅에 필요
    @NoArgsConstructor // Jackson 역직렬화 시 기본 생성자 필요
    @AllArgsConstructor
    @Builder
    public static class TagsDto {
        private String type;
        private String category;
        private String object;
        private String urgency;
    }
}