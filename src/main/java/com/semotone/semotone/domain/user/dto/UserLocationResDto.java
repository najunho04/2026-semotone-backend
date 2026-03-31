package com.semotone.semotone.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserLocationResDto {
    private String uid;
    private double latitude;
    private double longitude;
}
