package com.semotone.semotone.domain.post.dto;

import com.semotone.semotone.domain.post.entity.PostEntity;
import lombok.Builder;
import lombok.Getter;

// 게시글 목록/상세 조회 응답 시
@Getter
@Builder
public class PostResDto {
    private String postId; // Firestore 문서 ID
    private String userId; // 작성자
    private String title;
    private String content;
    private long createdAt;
    private boolean isAccept;
    private String accepted_userId;
    private double latitude;
    private double longitude;

    // Entity -> DTO 변환 정적 메서드
    public static PostResDto fromEntity(String postId, PostEntity entity) {
        return PostResDto.builder()
                .postId(postId)
                .userId(entity.getUserId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .createdAt(entity.getIsCreated() != null ? entity.getIsCreated().toDate().getTime() : 0L)
                .isAccept(entity.isAccept())
                .accepted_userId(entity.getAccepted_userId())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .build();
    }
}