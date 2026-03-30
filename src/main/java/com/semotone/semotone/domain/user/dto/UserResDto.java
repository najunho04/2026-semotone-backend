package com.semotone.semotone.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

// 유저 정보 응답 시
@Getter
@Builder
public class UserResDto {
    private String uid;
    private String nickName;
    private String gmail;
    private int point;
    private int acceptCount;
    // myPosts 리스트나 currentLocation 등 필요한 정보만 선택적으로 담아 응답
}