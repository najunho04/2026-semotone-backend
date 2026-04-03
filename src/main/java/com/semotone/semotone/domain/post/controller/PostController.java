package com.semotone.semotone.domain.post.controller;

import com.semotone.semotone.domain.ai.dto.AiResultResDto;
import com.semotone.semotone.domain.ai.service.AiService;
import com.semotone.semotone.domain.post.dto.PostAcceptReqDto;
import com.semotone.semotone.domain.post.dto.PostCompleteReqDto;
import com.semotone.semotone.domain.post.dto.PostCreateReqDto;
import com.semotone.semotone.domain.post.dto.PostResDto;
import com.semotone.semotone.domain.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final AiService aiService;

    @PostMapping("/create")
    public ResponseEntity<String> createPost(@RequestBody PostCreateReqDto reqDto) {
        try {
            String postId = postService.createPost(reqDto);
            return ResponseEntity.ok("게시글 생성 성공! 문서 ID: " + postId);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.internalServerError().body("게시글 생성 실패: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getPostList(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "distance") String sortBy) {
        try {
            List<PostResDto> list = postService.getPostList(lat, lng, sortBy);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("목록 조회 실패: " + e.getMessage());
        }
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostDetail(@PathVariable String postId) {
        try {
            PostResDto resDto = postService.getPostDetail(postId);
            return ResponseEntity.ok(resDto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("상세 조회 실패: " + e.getMessage());
        }
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    @PostMapping("/{postId}/accept")
    public ResponseEntity<String> acceptPost(
            @PathVariable String postId,
            @RequestBody PostAcceptReqDto reqDto) {
        try {
            postService.acceptPost(postId, reqDto.getAcceptingUserId());
            return ResponseEntity.ok("게시글 수락 성공! 완료 처리 시 포인트가 지급됩니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.internalServerError().body("게시글 수락 실패: " + e.getMessage());
        }
    }

    @PostMapping("/{postId}/complete")
    public ResponseEntity<String> completePost(
            @PathVariable String postId,
            @RequestBody PostCompleteReqDto reqDto) {
        try {
            postService.completePost(postId, reqDto.getRequesterUserId());
            return ResponseEntity.ok("게시글 완료 성공! 수락자에게 포인트가 지급되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.internalServerError().body("게시글 완료 실패: " + e.getMessage());
        }
    }
}
