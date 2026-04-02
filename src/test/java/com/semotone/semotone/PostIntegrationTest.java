package com.semotone.semotone;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.semotone.semotone.config.TestConfig;
import com.semotone.semotone.domain.post.dto.PostCreateReqDto;
import com.semotone.semotone.domain.post.repository.PostRepository;
import com.semotone.semotone.domain.user.entity.UserEntity;
import com.semotone.semotone.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Import(TestConfig.class)
class PostIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    private String testUserId;
    private String createdPostId;

    @BeforeEach
    void setUp() {
        testUserId = "post-integration-user-" + UUID.randomUUID();
        userRepository.save(testUserId, UserEntity.builder()
                .userId(testUserId)
                .nickName("integration-user")
                .gmail(testUserId + "@test.com")
                .point(1000)
                .acceptCount(0)
                .latitude(37.5665)
                .longitude(126.9780)
                .build());
    }

    @AfterEach
    void tearDown() throws Exception {
        if (createdPostId != null) {
            postRepository.delete(createdPostId);
        }
        if (testUserId != null) {
            userRepository.getDb().collection("users").document(testUserId).delete().get();
        }
    }

    @Test
    @DisplayName("게시글 생성 통합 테스트는 실제 Firestore 저장까지 검증한다")
    void createPost_Integration() throws Exception {
        PostCreateReqDto reqDto = new PostCreateReqDto();
        reqDto.setUserId(testUserId);
        reqDto.setTitle("통합 테스트 제목");
        reqDto.setContent("실제 서비스와 Firestore를 사용하는 통합 테스트입니다.");
        reqDto.setRewardPoint(100);
        reqDto.setLatitude(37.5665);
        reqDto.setLongitude(126.9780);

        String response = mockMvc.perform(post("/posts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("게시글 생성 성공! 문서 ID: ")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        createdPostId = response.split("문서 ID: ")[1];
    }
}
