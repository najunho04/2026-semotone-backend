package com.semotone.semotone.domain.ai.dto;

import com.semotone.semotone.domain.ai.entity.AiResultEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiResultResDto {
    private String postId;
    private String category;
    private String object;
    private String type;
    private String urgency;
    private String fromLocation;
    private String toLocation;

    private double postLatitude; //post에서 가져옴
    private double postLongitude; //post에서 가져옴

    private TagsDto tags; // 계층 구조 유지

    @Getter
    @Builder
    public static class TagsDto {
        private String type;
        private String category;
        private String object;
        private String urgency;
    }

    // Entity -> DTO 변환 메서드
    public static AiResultResDto fromEntity(AiResultEntity entity) {
        if (entity == null) return null;

        return AiResultResDto.builder()
                .postId(entity.getPostId())
                .category(entity.getCategory())
                .object(entity.getObject())
                .type(entity.getType())
                .urgency(entity.getUrgency())
                .fromLocation(entity.getFromLocation())
                .toLocation(entity.getToLocation())
                .postLatitude(entity.getPostLatitude())
                .postLongitude(entity.getPostLongitude())
                .tags(TagsDto.builder()
                        .type(entity.getTags().getType())
                        .category(entity.getTags().getCategory())
                        .object(entity.getTags().getObject())
                        .urgency(entity.getTags().getUrgency())
                        .build())
                .build();
    }
}