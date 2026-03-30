package com.semotone.semotone.domain.user.controller;

import com.semotone.semotone.domain.user.dto.UserCreateReqDto;
import com.semotone.semotone.domain.user.dto.UserResDto;
import com.semotone.semotone.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users") // 클래스 레벨에 공통 URL을 빼둡니다.
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // POST http://localhost:8080/api/users (회원가입)
    @PostMapping
    public ResponseEntity<UserResDto> signUp(
            @RequestBody UserCreateReqDto dto,
            Authentication authentication // JwtFilter가 넣어준 토큰 정보 보관함!
    ) {
        // 보관함에서 UID와 이메일을 쏙 꺼냅니다.
        String uid = (String) authentication.getPrincipal();
        String email = (String) authentication.getCredentials();

        // Service에 3가지 재료(uid, email, dto)를 던져주고 결과를 받습니다.
        UserResDto response = userService.signUp(uid, email, dto);

        return ResponseEntity.ok(response); // HTTP 200 상태코드와 함께 응답!
    }
}
