package com.semotone.semotone.domain.user.dto;

import lombok.Getter;

@Getter
public class UserCreateReqDto {
    // ID(이메일), 비밀번호, UID는 모두 토큰에서 뽑아낼 것이므로 여기서 받지 않습니다!
    // JWT (Firebase 인증 토큰)은 Header에서 클라이언트가 보낼 예정
    private String nickName;
    private double latitude;
    private double longitude;
}