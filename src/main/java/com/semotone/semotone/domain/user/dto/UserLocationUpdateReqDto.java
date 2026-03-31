package com.semotone.semotone.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // JSON 파싱을 위한 기본 생성자
public class UserLocationUpdateReqDto {
    private double latitude;
    private double longitude;
}