package com.semotone.semotone.domain.ai.controller;

import com.semotone.semotone.domain.ai.dto.AiResultResDto;
import com.semotone.semotone.domain.ai.entity.AiResultEntity;
import com.semotone.semotone.domain.ai.service.AiService;
import com.semotone.semotone.domain.user.dto.UserResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @GetMapping("/{postId}")
    public ResponseEntity<?> getAiResult(@PathVariable String postId) {
        try {
            AiResultResDto aiResult = aiService.getAiResult(postId);
            if (aiResult == null) {
                return ResponseEntity.ok(AiResultResDto.builder().build());
            }
            return ResponseEntity.ok(aiResult);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("AI 결과 조회 실패: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<AiResultResDto>> getAllAiResult(Authentication authentication) {
        return ResponseEntity.ok(aiService.getAllAiResult());
    }
}
