package com.semotone.semotone.domain.post.entity;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.GeoPoint;
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
    private String userId; // 작성자 UID
    private String title;
    private String content;
    private Timestamp isCreated; // Firestore 전용 타임스탬프
    private boolean isDelete;
    private boolean isAccept;

    private String accepted_userId; // 수락한 유저의 UID (초기엔 null)
    private double latitude;
    private double longitude;
}