package com.semotone.semotone.domain.ai.entity;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiResultEntity {

    @DocumentId
    private String postId; // 연결된 게시글의 ID (1:1 매칭)

    //AiResDto에서 가져옴
    private String category;
    private String object;
    private String type;
    private String urgency;
    private String fromLocation;
    private String toLocation;

    private double postLatitude; //post에서 가져옴
    private double postLongitude; //post에서 가져옴

    // 중첩된 객체로 'tags' 필드를 추가 - AiResDto에서 가져옴
    private AiTags tags;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AiTags {
        private String type;     // 예: "물건전달"
        private String category; // 예: "배달"
        private String object;   // 예: "책"
        private String urgency;  // 예: "보통"
    }
}