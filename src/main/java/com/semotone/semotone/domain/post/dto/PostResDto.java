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
    private long createdAt; // 클라이언트가 파싱하기 쉽게 Epoch Time(Millis)으로 변환해서 주는 것이 좋습니다.
    private boolean isAccept;
    private String acceptedUserId;
    private double latitude;
    private double longitude;

    // Entity -> DTO 변환 정적 메서드 (Service에서 사용하면 매우 편함)
    public static PostResDto fromEntity(String postId, PostEntity entity) {
        return PostResDto.builder()
                .postId(postId)
                .userId(entity.getUserId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .createdAt(entity.getIsCreated().toDate().getTime())
                .isAccept(entity.isAccept())
                .acceptedUserId(entity.getAcceptedUserId())
                .latitude(entity.getLocation().getLatitude())
                .longitude(entity.getLocation().getLongitude())
                .build();
    }
}