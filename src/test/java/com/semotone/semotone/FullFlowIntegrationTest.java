package com.semotone.semotone;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.semotone.semotone.domain.ai.dto.AiResultResDto;
import com.semotone.semotone.domain.ai.repository.aiRepository;
import com.semotone.semotone.domain.post.dto.PostCreateReqDto;
import com.semotone.semotone.domain.post.entity.PostEntity;
import com.semotone.semotone.domain.post.repository.PostRepository;
import com.semotone.semotone.domain.user.entity.UserEntity;
import com.semotone.semotone.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

/**
 * 실제 Gemini API를 사용하여 전체 플로우를 검증하는 통합 테스트
 *
 * Step 1: 유저 A, 유저 B 생성 → Firestore 저장 확인
 * Step 2: 유저 A가 게시물 작성 → 실제 Gemini AI 분석 → AI 결과 Firestore 저장
 * Step 3: 유저 B가 게시물 수락 → 완료 처리 → 포인트 지급 확인
 *
 * 주의: TestConfig를 Import하지 않음 (Mock 사용 안 함) → 실제 GeminiClient 호출
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FullFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private aiRepository aiRepository;

    private String requesterUserId;
    private String helperUserId;
    private String createdPostId;

    @BeforeEach
    void setUp() {
        requesterUserId = "requester-" + UUID.randomUUID();
        helperUserId = "helper-" + UUID.randomUUID();
    }

    @AfterEach
    void tearDown() throws Exception {
        try {
            // AI 결과 삭제
            if (createdPostId != null) {
                try {
                    userRepository.getDb().collection("ai_results").document(createdPostId).delete().get();
                } catch (Exception e) {
                    // AI 결과가 없을 수 있음
                }
            }

            // 게시물 삭제
            if (createdPostId != null) {
                postRepository.delete(createdPostId);
            }

            // 유저 삭제
            if (requesterUserId != null) {
                userRepository.getDb().collection("users").document(requesterUserId).delete().get();
            }
            if (helperUserId != null) {
                userRepository.getDb().collection("users").document(helperUserId).delete().get();
            }
        } catch (Exception e) {
            System.err.println("Cleanup failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Step 1: 유저 A와 유저 B를 생성하고 Firestore에 저장")
    void step1_CreateUsers() throws Exception {
        // 유저 A 생성
        UserEntity requesterUser = UserEntity.builder()
                .userId(requesterUserId)
                .nickName("유저A")
                .gmail(requesterUserId + "@test.com")
                .point(1000)
                .acceptCount(0)
                .latitude(37.5665)
                .longitude(126.9780)
                .build();
        userRepository.save(requesterUserId, requesterUser);

        // 유저 B 생성
        UserEntity helperUser = UserEntity.builder()
                .userId(helperUserId)
                .nickName("유저B")
                .gmail(helperUserId + "@test.com")
                .point(1000)
                .acceptCount(0)
                .latitude(37.5666)
                .longitude(126.9781)
                .build();
        userRepository.save(helperUserId, helperUser);

        // 검증: 유저가 Firestore에 저장되었는지 확인
        UserEntity savedRequester = userRepository.findById(requesterUserId).orElse(null);
        UserEntity savedHelper = userRepository.findById(helperUserId).orElse(null);

        assertThat(savedRequester).isNotNull();
        assertThat(savedRequester.getPoint()).isEqualTo(1000);
        assertThat(savedRequester.getAcceptCount()).isEqualTo(0);

        assertThat(savedHelper).isNotNull();
        assertThat(savedHelper.getPoint()).isEqualTo(1000);
        assertThat(savedHelper.getAcceptCount()).isEqualTo(0);

        System.out.println("✓ Step 1 완료: 유저 생성");
    }

    @Test
    @Order(2)
    @DisplayName("Step 2: 유저 A가 게시물 작성 → 실제 Gemini API 호출 → AI 결과 저장")
    void step2_CreatePostWithAI() throws Exception {
        // 먼저 유저를 생성
        UserEntity requesterUser = UserEntity.builder()
                .userId(requesterUserId)
                .nickName("유저A")
                .gmail(requesterUserId + "@test.com")
                .point(1000)
                .acceptCount(0)
                .latitude(37.5665)
                .longitude(126.9780)
                .build();
        userRepository.save(requesterUserId, requesterUser);

        // 게시물 작성 요청 (실제 Gemini API 호출이 여기서 발생)
        PostCreateReqDto reqDto = new PostCreateReqDto();
        reqDto.setUserId(requesterUserId);
        reqDto.setTitle("도서관 책 배달");
        reqDto.setContent("전자정보관에서 중앙도서관으로 책 한 권 배달해주세요");
        reqDto.setRewardPoint(200);
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
        System.out.println("✓ 게시물 생성 완료, postId: " + createdPostId);

        // 검증 1: 게시물이 Firestore에 저장되었는지 확인
        PostEntity savedPost = postRepository.findById(createdPostId);
        assertThat(savedPost).isNotNull();
        assertThat(savedPost.getUserId()).isEqualTo(requesterUserId);
        assertThat(savedPost.getRewardPoint()).isEqualTo(200);
        assertThat(savedPost.isAccepted()).isFalse();
        assertThat(savedPost.isCompleted()).isFalse();
        System.out.println("✓ 게시물 Firestore 저장 확인");

        // 검증 2: 유저 A의 포인트가 차감되었는지 확인 (1000 - 200 = 800)
        UserEntity updatedRequester = userRepository.findById(requesterUserId).orElse(null);
        assertThat(updatedRequester).isNotNull();
        assertThat(updatedRequester.getPoint()).isEqualTo(800);
        System.out.println("✓ 유저 A 포인트 차감 확인: 1000 → 800");

        // 검증 3: 유저 A의 myPosts에 postId가 추가되었는지 확인
        assertThat(updatedRequester.getMyPosts()).contains(createdPostId);
        System.out.println("✓ 유저 A myPosts에 postId 추가 확인");

        // 검증 4: AI 결과가 ai_results 컬렉션에 저장되었는지 확인
        Thread.sleep(2000); // Firestore 저장 대기
        String aiResponseJson = mockMvc.perform(get("/posts/{postId}/ai", createdPostId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AiResultResDto aiResult = objectMapper.readValue(aiResponseJson, AiResultResDto.class);
        assertThat(aiResult).isNotNull();
        assertThat(aiResult.getCategory()).isNotNull();
        assertThat(aiResult.getObject()).isNotNull();
        System.out.println("✓ AI 분석 결과 저장 확인");
        System.out.println("  - Category: " + aiResult.getCategory());
        System.out.println("  - Object: " + aiResult.getObject());
        System.out.println("✓ Step 2 완료: 게시물 작성 + AI 분석");
    }

    @Test
    @Order(3)
    @DisplayName("Step 3: 유저 B가 게시물 수락 → 완료 처리 → 포인트 지급")
    void step3_AcceptAndCompletePost() throws Exception {
        // 먼저 유저 2명과 게시물을 생성
        UserEntity requesterUser = UserEntity.builder()
                .userId(requesterUserId)
                .nickName("유저A")
                .gmail(requesterUserId + "@test.com")
                .point(1000)
                .acceptCount(0)
                .latitude(37.5665)
                .longitude(126.9780)
                .build();
        userRepository.save(requesterUserId, requesterUser);

        UserEntity helperUser = UserEntity.builder()
                .userId(helperUserId)
                .nickName("유저B")
                .gmail(helperUserId + "@test.com")
                .point(1000)
                .acceptCount(0)
                .latitude(37.5666)
                .longitude(126.9781)
                .build();
        userRepository.save(helperUserId, helperUser);

        // 게시물 작성
        PostCreateReqDto reqDto = new PostCreateReqDto();
        reqDto.setUserId(requesterUserId);
        reqDto.setTitle("물건 배달");
        reqDto.setContent("중앙도서관에서 물건을 받아다 주세요");
        reqDto.setRewardPoint(200);
        reqDto.setLatitude(37.5665);
        reqDto.setLongitude(126.9780);

        String response = mockMvc.perform(post("/posts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        createdPostId = response.split("문서 ID: ")[1];
        System.out.println("✓ 게시물 생성 완료, postId: " + createdPostId);

        // Step 3-1: 유저 B가 게시물 수락
        String acceptJson = """
                {
                  "acceptingUserId": "%s"
                }
                """.formatted(helperUserId);

        mockMvc.perform(post("/posts/{postId}/accept", createdPostId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(acceptJson))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("완료 처리 시 포인트가 지급됩니다")));

        // 검증: 수락 후 유저 B의 포인트는 변하지 않아야 함 (1000 유지)
        UserEntity accepterAfterAccept = userRepository.findById(helperUserId).orElse(null);
        assertThat(accepterAfterAccept).isNotNull();
        assertThat(accepterAfterAccept.getPoint()).isEqualTo(1000);
        assertThat(accepterAfterAccept.getAcceptCount()).isEqualTo(0);

        // 게시물 상태 확인
        PostEntity acceptedPost = postRepository.findById(createdPostId);
        assertThat(acceptedPost.isAccepted()).isTrue();
        assertThat(acceptedPost.getAccepted_userId()).isEqualTo(helperUserId);
        System.out.println("✓ 게시물 수락 완료: accepted=true, accepted_userId=" + helperUserId);
        System.out.println("✓ 유저 B 포인트 미변화 확인: 1000 유지");

        // Step 3-2: 유저 A가 게시물 완료 처리
        String completeJson = """
                {
                  "requesterUserId": "%s"
                }
                """.formatted(requesterUserId);

        mockMvc.perform(post("/posts/{postId}/complete", createdPostId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completeJson))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("수락자에게 포인트가 지급되었습니다")));

        // 검증: 완료 후 유저 B의 포인트가 증가 (1000 + 200 = 1200)
        UserEntity accepterAfterComplete = userRepository.findById(helperUserId).orElse(null);
        assertThat(accepterAfterComplete).isNotNull();
        assertThat(accepterAfterComplete.getPoint()).isEqualTo(1200);
        assertThat(accepterAfterComplete.getAcceptCount()).isEqualTo(1);
        System.out.println("✓ 유저 B 포인트 지급 확인: 1000 → 1200");
        System.out.println("✓ 유저 B acceptCount 증가 확인: 0 → 1");

        // 게시물 완료 상태 확인
        PostEntity completedPost = postRepository.findById(createdPostId);
        assertThat(completedPost.isCompleted()).isTrue();
        assertThat(completedPost.isDeleted()).isTrue();
        System.out.println("✓ 게시물 완료 처리 확인: completed=true");

        // 유저 A의 최종 포인트 확인 (1000 - 200 = 800)
        UserEntity requesterAfterComplete = userRepository.findById(requesterUserId).orElse(null);
        assertThat(requesterAfterComplete).isNotNull();
        assertThat(requesterAfterComplete.getPoint()).isEqualTo(800);
        System.out.println("✓ 유저 A 최종 포인트 확인: 800");

        System.out.println("✓ Step 3 완료: 게시물 수락 + 완료 처리");
    }
}
