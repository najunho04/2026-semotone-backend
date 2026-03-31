package com.semotone.semotone.domain.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.semotone.semotone.domain.post.controller.PostController;
import com.semotone.semotone.domain.post.dto.PostCreateReqDto;
import com.semotone.semotone.domain.post.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class) // PostController 관련 설정만 로드
@AutoConfigureMockMvc(addFilters = false)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc; // HTTP 호출을 시뮬레이션하기 위한 객체

    @MockitoBean
    private PostService postService; // Service 계층을 Mock 객체로 주입

    @Autowired
    private ObjectMapper objectMapper; // 객체를 JSON으로 변환하기 위함

    @Test
    @DisplayName("게시글 생성 API 성공 테스트")
    void createPost_Success() throws Exception {
        // 1. Given: 테스트에 필요한 가공 데이터 준비
        PostCreateReqDto reqDto = new PostCreateReqDto();
        reqDto.setUserId("user123");
        reqDto.setTitle("테스트 제목");
        reqDto.setContent("테스트 내용");
        reqDto.setLatitude(37.1234);
        reqDto.setLongitude(127.1234);

        String mockPostId = "generated-firestore-id-123";

        // Service의 createPost 메서드가 호출되면 mockPostId를 반환하도록 설정
        given(postService.createPost(any(PostCreateReqDto.class))).willReturn(mockPostId);

        // 2. When & Then: API 호출 및 검증
        mockMvc.perform(post("/api/posts") // POST /api/posts 호출
                        .contentType(MediaType.APPLICATION_JSON) // JSON 형식으로 보냄
                        .content(objectMapper.writeValueAsString(reqDto))) // DTO를 JSON 문자열로 변환
                .andExpect(status().isOk()) // HTTP 200 응답 기대
                .andExpect(content().string("게시글 생성 성공! 문서 ID: " + mockPostId)); // 응답 메시지 검증
    }

    @Test
    @DisplayName("게시글 생성 API 실패 테스트 (서버 에러)")
    void createPost_Fail() throws Exception {
        // 1. Given: 에러가 발생하는 상황 설정
        given(postService.createPost(any(PostCreateReqDto.class)))
                .willThrow(new RuntimeException("Firebase 연결 실패"));

        PostCreateReqDto reqDto = new PostCreateReqDto();
        reqDto.setTitle("에러 테스트");

        // 2. When & Then
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isInternalServerError()); // HTTP 500 응답 기대
    }
}