package com.semotone.semotone.domain.post.dto;

import com.semotone.semotone.domain.post.entity.PostEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostResDto {
    private String postId;
    private String userId;
    private String title;
    private String content;
    private int rewardPoint;
    private long createdAt;
    private boolean isAccept;
    private boolean completed;
    private String accepted_userId;
    private double latitude;
    private double longitude;

    public static PostResDto fromEntity(String postId, PostEntity entity) {
        return PostResDto.builder()
                .postId(postId)
                .userId(entity.getUserId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .rewardPoint(entity.getRewardPoint())
                .createdAt(entity.getIsCreated() != null ? entity.getIsCreated().toDate().getTime() : 0L)
                .isAccept(entity.isAccepted())
                .completed(entity.isCompleted())
                .accepted_userId(entity.getAccepted_userId())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .build();
    }
}
