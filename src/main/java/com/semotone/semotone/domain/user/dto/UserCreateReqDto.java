package com.semotone.semotone.domain.user.dto;

import lombok.Getter;

@Getter
public class UserCreateReqDto {
    // 클라이언트에서 firebaseAuth에 직접 인증 후 토큰 발급 받음.
    // -> 받은 토큰으로 서버에 api 호출
    // -> 서버에서 토큰 인증 후 DB 저장

    // ID(이메일), 비밀번호, UID는 모두 토큰에서 뽑아낼 것이므로 여기서 받지 않습니다!
    // JWT (Firebase 인증 토큰)은 Header에서 클라이언트가 보낼 예정
    private String nickName;
    private double latitude;
    private double longitude;
    private String school;
    private String major;
}