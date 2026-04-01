package com.semotone.semotone.domain.user.controller;

import com.semotone.semotone.domain.user.dto.UserCreateReqDto;
import com.semotone.semotone.domain.user.dto.UserLocationResDto;
import com.semotone.semotone.domain.user.dto.UserLocationUpdateReqDto;
import com.semotone.semotone.domain.user.dto.UserResDto;
import com.semotone.semotone.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // POST http://localhost:8080/api/users (회원가입)
    @PostMapping("/auth/login")
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

    // PATCH http://localhost:8080/api/users/location
    // 위치 정보 동기화
    @PatchMapping("/users/location")
    public ResponseEntity<Void> updateLocation(
            @RequestBody UserLocationUpdateReqDto dto,
            Authentication authentication // JwtFilter가 검증해둔 토큰 정보
    ) {
        String uid = (String) authentication.getPrincipal(); // 토큰에서 UID 추출

        userService.updateUserLocation(uid, dto);

        // 데이터 반환 없이 '200 OK'만 빠르게 던져줍니다. (네트워크 비용 절약)
        return ResponseEntity.ok().build();
    }

    // GET http://localhost:8080/users/me
    // 본인 정보 가져오기
    @GetMapping("/users/me")
    public ResponseEntity<UserResDto> getMyProfile(
            Authentication authentication // 어김없이 등장하는 JwtFilter의 산물!
    ) {
        // 1. 토큰 보관함에서 내 고유 UID를 꺼냅니다. (안전함 보장)
        String uid = (String) authentication.getPrincipal();

        // 2. Service에게 내 UID를 주고 프로필(DTO)을 받아옵니다.
        UserResDto response = userService.getMyProfile(uid);

        // 3. 200 OK 상태 코드와 함께 JSON 데이터로 응답합니다.
        return ResponseEntity.ok(response);
    }

    // GET http://localhost:8080/users/{userId}
    @GetMapping("/users/{user_id}")
    public ResponseEntity<UserResDto> getUserProfile(
            Authentication authentication,
            @PathVariable String user_id
    ){
        // 2. Service에게 내 UID를 주고 프로필(DTO)을 받아옵니다.
        UserResDto response = userService.getUserProfile(user_id);

        // 3. 200 OK 상태 코드와 함께 JSON 데이터로 응답합니다.
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/me/location")
    public ResponseEntity<UserLocationResDto> getMyLocation(Authentication authentication){
        String uid = (String) authentication.getPrincipal();
        UserLocationResDto response = userService.getMyLocation(uid);
        return ResponseEntity.ok(response);
    }

    // GET http://localhost:8080/users
    // 전체 유저 목록 조회
    @GetMapping("/users")
    public ResponseEntity<List<UserResDto>> getAllUsers(Authentication authentication) {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // GET http://localhost:8080/users/locations
    // 전체 유저 위치 정보 조회
    @GetMapping("/users/all/location")
    public ResponseEntity<List<UserLocationResDto>> getAllUsersLocation(Authentication authentication) {
        return ResponseEntity.ok(userService.getAllUsersLocation());
    }
}
