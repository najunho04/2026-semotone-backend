package com.semotone.semotone.domain.chat.controller;

import com.semotone.semotone.domain.chat.dto.ChatRoomCreateRequest;
import com.semotone.semotone.domain.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;


    @PostMapping("/rooms")
    public ResponseEntity<String> createRoom(
            Authentication authentication, // JWTFilter에서 넣어준 유저 식별자
            @RequestBody ChatRoomCreateRequest request) {

        String currentUserId = (String) authentication.getPrincipal();

        // 1. 방 생성 로직 호출
        String roomId = chatRoomService.createOrGetChatRoom(currentUserId, request.getAccepterId());

        // 2. 생성된 방의 ID를 Flutter로 리턴 (이 ID로 StreamBuilder 연결)
        return ResponseEntity.ok(roomId);
    }
}