package com.semotone.semotone; // 실제 경로에 맞게 확인해 주세요!

import com.fasterxml.jackson.databind.ObjectMapper;
import com.semotone.semotone.domain.post.dto.PostCreateReqDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest // 💡 핵심: 가짜 객체(Mock)가 아닌 실제 Service와 Repository를 모두 메모리에 올립니다.
@AutoConfigureMockMvc(addFilters = false) // 💡 Security 인증 필터를 꺼서 403 에러 방지
class PostIntegrationTest {

    @Autowired
    private MockMvc mockMvc; // API 호출을 위한 객체

    @Autowired
    private ObjectMapper objectMapper; // JSON 변환용

    @Test
    @DisplayName("게시글 생성 통합 테스트 - 실제 Firebase에 데이터가 저장되어야 함")
    void createPost_Integration() throws Exception {
        // 1. Given: 실제 저장할 테스트 데이터 준비
        PostCreateReqDto reqDto = new PostCreateReqDto();
        reqDto.setUserId("integration-test-user");
        reqDto.setTitle("통합 테스트 제목입니다");
        reqDto.setContent("이 데이터는 통합 테스트를 통해 실제 Firebase에 저장됩니다.");
        reqDto.setLatitude(37.5665);
        reqDto.setLongitude(126.9780);

        // 2. When & Then: API 호출 및 응답 검증
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isOk()) // HTTP 200 성공을 기대함
                // 실제 Firebase 문서 ID는 랜덤이므로 "게시글 생성 성공! 문서 ID: " 문자열이 포함되었는지(containsString)만 확인합니다.
                .andExpect(content().string(containsString("게시글 생성 성공! 문서 ID: ")));
    }
}