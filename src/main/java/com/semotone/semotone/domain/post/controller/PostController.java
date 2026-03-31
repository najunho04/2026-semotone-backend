package com.semotone.semotone.domain.post.controller;

import com.semotone.semotone.domain.post.dto.PostAcceptReqDto;
import com.semotone.semotone.domain.post.dto.PostCreateReqDto;
import com.semotone.semotone.domain.post.service.PostService;
import com.semotone.semotone.domain.post.service.PostServiceImpl;
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

    @PostMapping("/{postId}/accept")
    public ResponseEntity<String> acceptPost(
            @PathVariable String postId,
            @RequestBody PostAcceptReqDto reqDto) {
        try {
            // Service에서 게시글 수락 처리 (포인트/acceptCount 증가)
            postService.acceptPost(postId, reqDto.getAcceptingUserId());
            return ResponseEntity.ok("게시글 수락 성공! 포인트와 수락 횟수가 증가했습니다.");
        } catch (RuntimeException e) {
            // 이미 수락된 게시글 → 400 Bad Request
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ExecutionException | InterruptedException e) {
            // Firestore 에러 → 500 Internal Server Error
            return ResponseEntity.internalServerError().body("게시글 수락 실패: " + e.getMessage());
        }
    }

}
