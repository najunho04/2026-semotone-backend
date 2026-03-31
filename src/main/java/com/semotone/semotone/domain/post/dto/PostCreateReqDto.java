package com.semotone.semotone.domain.post.dto;

import lombok.Getter;
import lombok.Setter;

// 게시글 작성 요청 시 (보안을 위해 userId는 여기서 받지 않음)
@Getter
@Setter
public class PostCreateReqDto {
    private String userId;
    private String title;
    private String content;
    private double latitude; // Flutter에서 넘겨주는 위도
    private double longitude; // Flutter에서 넘겨주는 경도
}
