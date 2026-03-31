package com.semotone.semotone;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.semotone.semotone.domain.post.controller.PostController;
import com.semotone.semotone.domain.post.dto.PostCreateReqDto;
import com.semotone.semotone.domain.post.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @SpringBootTest 대신 컨트롤러(API)만 가볍게 테스트하는 어노테이션 사용
@WebMvcTest(controllers = PostController.class)
@AutoConfigureMockMvc(addFilters = false) // 시큐리티 인증 무시
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 핵심: 실제 Firebase DB와 연결되는 Service 대신 '가짜(Mock)' Service를 주입
    @MockitoBean
    private PostService postService;

    @Test
    @DisplayName("게시글 생성 API가 정상 작동하는지 테스트")
    void createPostTest() throws Exception {
        // 1. 클라이언트가 보낼 데이터(DTO) 세팅
        PostCreateReqDto reqDto = new PostCreateReqDto();
        reqDto.setUserId("testUser_999");
        reqDto.setTitle("테스트 코드로 작성한 글");
        reqDto.setContent("포스트맨 없이 테스트 코드로 실행했습니다!");
        reqDto.setLatitude(37.5665);
        reqDto.setLongitude(126.9780);

        // 💡 가짜 Service가 동작할 행동을 미리 설정 (에러 방지용)
        Mockito.when(postService.createPost(any(PostCreateReqDto.class)))
                .thenReturn("가짜_문서_ID_12345");

        // 2. DTO 객체를 JSON 형태의 문자열로 변환
        String jsonBody = objectMapper.writeValueAsString(reqDto);

        // 3. MockMvc를 사용해 /api/posts 주소로 POST 요청 날리기
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk()); // 응답 상태 코드가 200(OK)인지 확인
    }
}
