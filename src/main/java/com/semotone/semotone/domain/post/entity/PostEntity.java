package com.semotone.semotone.domain.post.entity;

import com.google.cloud.Timestamp;
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
public class PostEntity {

    @DocumentId
    private String id;
    private String userId;
    private String title;
    private String content;
    private int rewardPoint;
    private Timestamp isCreated;
    private boolean deleted;
    private boolean accepted;
    private boolean completed;
    private String accepted_userId;
    private double latitude;
    private double longitude;
}
