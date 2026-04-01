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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Import(TestConfig.class)
class UserAndPostIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    private final List<String> createdPostIds = new ArrayList<>();
    private String requesterUserId;
    private String helperUserId;

    @BeforeEach
    void setUp() {
        requesterUserId = "requester-" + UUID.randomUUID();
        helperUserId = "helper-" + UUID.randomUUID();

        userRepository.save(requesterUserId, UserEntity.builder()
                .userId(requesterUserId)
                .nickName("requester")
                .gmail(requesterUserId + "@test.com")
                .point(1000)
                .acceptCount(0)
                .latitude(37.2479)
                .longitude(127.0770)
                .build());

        userRepository.save(helperUserId, UserEntity.builder()
                .userId(helperUserId)
                .nickName("helper")
                .gmail(helperUserId + "@test.com")
                .point(1000)
                .acceptCount(0)
                .latitude(37.2480)
                .longitude(127.0771)
                .build());
    }

    @AfterEach
    void tearDown() throws Exception {
        for (String postId : createdPostIds) {
            postRepository.delete(postId);
        }

        if (requesterUserId != null) {
            userRepository.getDb().collection("users").document(requesterUserId).delete().get();
        }
        if (helperUserId != null) {
            userRepository.getDb().collection("users").document(helperUserId).delete().get();
        }
    }

    @Test
    @DisplayName("게시글 생성 시 설정한 포인트만큼 작성자 포인트가 차감된다")
    void createPostWithRewardPoint_Success() throws Exception {
        PostCreateReqDto reqDto = createRequest(requesterUserId, 150);

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
        assertThat(savedPost.getRewardPoint()).isEqualTo(150);
        assertThat(savedPost.isAccepted()).isFalse();
        assertThat(savedPost.isCompleted()).isFalse();
        assertThat(userRepository.findById(requesterUserId).orElseThrow().getPoint()).isEqualTo(850);
    }

    @Test
    @DisplayName("게시글 수락 시에는 포인트가 지급되지 않고 상태만 변경된다")
    void acceptPost_DoesNotPayImmediately() throws Exception {
        String postId = createPost(requesterUserId, 120);
        int helperPointBefore = userRepository.findById(helperUserId).orElseThrow().getPoint();
        int helperAcceptBefore = userRepository.findById(helperUserId).orElseThrow().getAcceptCount();

        String acceptJson = """
                {
                  "acceptingUserId": "%s"
                }
                """.formatted(helperUserId);

        mockMvc.perform(post("/posts/{postId}/accept", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(acceptJson))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("완료 처리 시 포인트가 지급됩니다")));

        PostEntity acceptedPost = postRepository.findById(postId);
        assertThat(acceptedPost.isAccepted()).isTrue();
        assertThat(acceptedPost.getAccepted_userId()).isEqualTo(helperUserId);
        assertThat(userRepository.findById(helperUserId).orElseThrow().getPoint()).isEqualTo(helperPointBefore);
        assertThat(userRepository.findById(helperUserId).orElseThrow().getAcceptCount()).isEqualTo(helperAcceptBefore);
    }

    @Test
    @DisplayName("작성자가 완료 처리하면 수락자에게 포인트가 지급되고 acceptCount가 증가한다")
    void completePost_PaysRewardToAcceptedUser() throws Exception {
        String postId = createPost(requesterUserId, 200);

        String acceptJson = """
                {
                  "acceptingUserId": "%s"
                }
                """.formatted(helperUserId);

        mockMvc.perform(post("/posts/{postId}/accept", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(acceptJson))
                .andExpect(status().isOk());

        String completeJson = """
                {
                  "requesterUserId": "%s"
                }
                """.formatted(requesterUserId);

        mockMvc.perform(post("/posts/{postId}/complete", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completeJson))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("수락자에게 포인트가 지급되었습니다")));

        PostEntity completedPost = postRepository.findById(postId);
        assertThat(completedPost.isCompleted()).isTrue();
        assertThat(completedPost.isDeleted()).isTrue();
        assertThat(userRepository.findById(helperUserId).orElseThrow().getPoint()).isEqualTo(1200);
        assertThat(userRepository.findById(helperUserId).orElseThrow().getAcceptCount()).isEqualTo(1);
        assertThat(userRepository.findById(requesterUserId).orElseThrow().getPoint()).isEqualTo(800);
    }

    @Test
    @DisplayName("게시글 상세 조회에는 rewardPoint와 완료 상태가 포함된다")
    void getPostDetail_ContainsRewardPointAndCompleted() throws Exception {
        String postId = createPost(requesterUserId, 90);

        mockMvc.perform(get("/posts/{postId}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rewardPoint").value(90))
                .andExpect(jsonPath("$.completed").value(false));
    }

    private PostCreateReqDto createRequest(String userId, int rewardPoint) {
        PostCreateReqDto reqDto = new PostCreateReqDto();
        reqDto.setUserId(userId);
        reqDto.setTitle("심부름 요청");
        reqDto.setContent("중앙도서관에서 물건을 받아다 주세요.");
        reqDto.setRewardPoint(rewardPoint);
        reqDto.setLatitude(37.2479);
        reqDto.setLongitude(127.0770);
        return reqDto;
    }

    private String createPost(String userId, int rewardPoint) throws Exception {
        String response = mockMvc.perform(post("/posts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest(userId, rewardPoint))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String postId = response.split("문서 ID: ")[1];
        createdPostIds.add(postId);
        return postId;
    }
}
