package com.semotone.semotone.domain.post.dto;

import lombok.Getter;
import lombok.Setter;

// 게시글 수락 요청 시
@Getter
@Setter
public class PostAcceptReqDto {
    private String acceptingUserId; // 수락하는 유저의 UID
}
