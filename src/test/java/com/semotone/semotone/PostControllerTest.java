package com.semotone.semotone;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.semotone.semotone.config.TestConfig;
import com.semotone.semotone.domain.post.dto.PostCreateReqDto;
import com.semotone.semotone.domain.post.entity.PostEntity;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Import(TestConfig.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    private final List<String> createdPostIds = new ArrayList<>();
    private String testUserId;

    @BeforeEach
    void setUp() {
        testUserId = "post-controller-user-" + UUID.randomUUID();
        userRepository.save(testUserId, UserEntity.builder()
                .userId(testUserId)
                .nickName("controller-user")
                .gmail(testUserId + "@test.com")
                .point(1000)
                .acceptCount(0)
                .latitude(37.5665)
                .longitude(126.9780)
                .build());
    }

    @AfterEach
    void tearDown() throws Exception {
        for (String postId : createdPostIds) {
            postRepository.delete(postId);
        }
        if (testUserId != null) {
            userRepository.getDb().collection("users").document(testUserId).delete().get();
        }
    }

    @Test
    @DisplayName("게시글 생성 API는 실제 서비스와 Firestore를 통해 저장된다")
    void createPostTest() throws Exception {
        PostCreateReqDto reqDto = new PostCreateReqDto();
        reqDto.setUserId(testUserId);
        reqDto.setTitle("테스트 제목");
        reqDto.setContent("테스트 본문");
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

        String postId = response.split("문서 ID: ")[1];
        createdPostIds.add(postId);

        PostEntity savedPost = postRepository.findById(postId);
        assertThat(savedPost).isNotNull();
        assertThat(savedPost.getRewardPoint()).isEqualTo(100);
        assertThat(savedPost.getUserId()).isEqualTo(testUserId);
        assertThat(userRepository.findById(testUserId).orElseThrow().getPoint()).isEqualTo(900);
    }
}
