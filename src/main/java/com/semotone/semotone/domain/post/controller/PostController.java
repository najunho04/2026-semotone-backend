package com.semotone.semotone.domain.post.controller;

import com.semotone.semotone.domain.post.dto.PostCreateReqDto;
import com.semotone.semotone.domain.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor

public class PostController {
    private final PostService postService;
    @PostMapping
    public ResponseEntity<String> createPost(@RequestBody PostCreateReqDto reqDto) {
        try {
            // Service에 처리를 맡기고 생성된 ID를 받아옴
            String postId = postService.createPost(reqDto);
            return ResponseEntity.ok("게시글 생성 성공! 문서 ID: " + postId);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.internalServerError().body("게시글 생성 실패: " + e.getMessage());
        }
    }


}
