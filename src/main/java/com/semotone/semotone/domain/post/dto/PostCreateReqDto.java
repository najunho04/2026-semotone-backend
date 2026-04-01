package com.semotone.semotone.domain.post.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostCreateReqDto {
    private String userId;
    private String title;
    private String content;
    private int rewardPoint;
    private double latitude;
    private double longitude;
}
