package com.semotone.semotone.domain.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.semotone.semotone.config.TestConfig;
import com.semotone.semotone.domain.post.dto.PostCreateReqDto;
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
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private String lowPointUserId;

    @BeforeEach
    void setUp() {
        lowPointUserId = "post-controller-low-point-" + UUID.randomUUID();
        userRepository.save(lowPointUserId, UserEntity.builder()
                .userId(lowPointUserId)
                .nickName("low-point-user")
                .gmail(lowPointUserId + "@test.com")
                .point(50)
                .acceptCount(0)
                .latitude(37.1234)
                .longitude(127.1234)
                .build());
    }

    @AfterEach
    void tearDown() throws Exception {
        if (lowPointUserId != null) {
            userRepository.getDb().collection("users").document(lowPointUserId).delete().get();
        }
    }

    @Test
    @DisplayName("포인트가 부족하면 게시글 생성 API는 400을 반환한다")
    void createPost_Fail() throws Exception {
        PostCreateReqDto reqDto = new PostCreateReqDto();
        reqDto.setUserId(lowPointUserId);
        reqDto.setTitle("에러 테스트");
        reqDto.setContent("포인트 부족 테스트");
        reqDto.setRewardPoint(100);
        reqDto.setLatitude(37.1234);
        reqDto.setLongitude(127.1234);

        mockMvc.perform(post("/posts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("포인트가 부족합니다.")));
    }
}
