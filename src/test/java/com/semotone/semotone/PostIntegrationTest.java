package com.semotone.semotone;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.semotone.semotone.auth.FirebaseAuthTokenProvider;
import com.semotone.semotone.domain.user.entity.UserEntity;
import com.semotone.semotone.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PostIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private String realJwtToken;
    private String testUid; // 게시글 작성자 UID (토큰 기반)

    // tearDown에서 생성된 데이터를 일괄 삭제하기 위한 추적 목록
    private final List<String> createdPostIds = new ArrayList<>();
    private final List<String> createdUserIds = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        FirebaseAuthTokenProvider provider = new FirebaseAuthTokenProvider();
        realJwtToken = provider.getTestToken("test@example.com", "password123");

        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(realJwtToken);
        testUid = decodedToken.getUid();

        createdPostIds.clear();
        createdUserIds.clear();
    }

    @AfterEach
    void tearDown() {
        // 테스트 중 생성된 게시글 전체 삭제
        for (String postId : createdPostIds) {
            try {
                userRepository.getDb().collection("posts").document(postId).delete().get();
            } catch (Exception e) {
                System.err.println("게시글 삭제 실패 [ID: " + postId + "]: " + e.getMessage());
            }
        }

        // 테스트 중 생성된 유저 전체 삭제 (testUid 포함)
        createdUserIds.add(testUid);
        for (String uid : createdUserIds) {
            try {
                userRepository.getDb().collection("users").document(uid).delete().get();
            } catch (Exception e) {
                System.err.println("유저 삭제 실패 [UID: " + uid + "]: " + e.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────
    // 공통 헬퍼 메서드
    // ─────────────────────────────────────────────

    /** 테스트용 유저를 Firestore에 저장하고 createdUserIds에 등록 */
    private void saveTestUser(String uid, String nickName) {
        UserEntity user = UserEntity.builder()
                .nickName(nickName)
                .gmail("test_" + uid + "@example.com")
                .point(1000)
                .acceptCount(0)
                .build();
        userRepository.save(uid, user);
        createdUserIds.add(uid);
    }

    /** HTTP 요청으로 게시글을 생성하고 생성된 postId를 반환 */
    private String createPost(String authorUid) throws Exception {
        String requestBody = """
                {
                    "userId": "%s",
                    "title": "통합테스트 게시글",
                    "content": "통합테스트 내용입니다.",
                    "latitude": 37.5665,
                    "longitude": 126.9780
                }
                """.formatted(authorUid);

        MvcResult result = mockMvc.perform(
                        post("/api/posts")
                                .header("Authorization", "Bearer " + realJwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        // 응답: "게시글 생성 성공! 문서 ID: {postId}" → postId 파싱
        String postId = result.getResponse().getContentAsString()
                .replace("게시글 생성 성공! 문서 ID: ", "").trim();
        createdPostIds.add(postId);
        return postId;
    }

    // ─────────────────────────────────────────────
    // 테스트 케이스
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("게시글 생성 통합 테스트: 게시글이 Firestore에 저장되고 작성자의 myPosts 리스트에 postId가 추가되어야 한다.")
    void createPostIntegrationTest() throws Exception {
        // given - 게시글 작성자 유저 생성
        saveTestUser(testUid, "게시글작성자");

        // when - 게시글 생성 HTTP 요청
        String postId = createPost(testUid);

        // then - 작성자의 myPosts에 postId가 추가되었는지 확인
        Optional<UserEntity> updatedAuthor = userRepository.findById(testUid);

        assertThat(updatedAuthor).isPresent();
        assertThat(updatedAuthor.get().getMyPosts()).contains(postId);
    }

    @Test
    @DisplayName("게시글 수락 통합 테스트: 수락 시 isAccept=true, accepted_userId 설정, 수락자 포인트 +10, acceptCount +1이 되어야 한다.")
    void acceptPostIntegrationTest() throws Exception {
        // given
        // 1. 게시글 작성자 & 수락자 유저 생성
        saveTestUser(testUid, "게시글작성자");
        String acceptorUid = "acceptor_" + UUID.randomUUID();
        saveTestUser(acceptorUid, "수락자");

        // 2. 게시글 생성
        String postId = createPost(testUid);

        // when - 게시글 수락 HTTP 요청
        String acceptRequestBody = """
                {
                    "acceptingUserId": "%s"
                }
                """.formatted(acceptorUid);

        mockMvc.perform(
                        post("/api/posts/" + postId + "/accept")
                                .header("Authorization", "Bearer " + realJwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(acceptRequestBody))
                .andExpect(status().isOk());

        // then
        // 1. Firestore에서 게시글 상태 직접 확인
        var postSnapshot = userRepository.getDb().collection("posts").document(postId).get().get();
        assertThat(postSnapshot.getBoolean("isAccept")).isTrue();
        assertThat(postSnapshot.getString("accepted_userId")).isEqualTo(acceptorUid);

        // 2. 수락자의 포인트와 acceptCount 증가 확인
        Optional<UserEntity> updatedAcceptor = userRepository.findById(acceptorUid);
        assertThat(updatedAcceptor).isPresent();
        assertThat(updatedAcceptor.get().getPoint()).isEqualTo(1010);       // 1000 + 10
        assertThat(updatedAcceptor.get().getAcceptCount()).isEqualTo(1);    // 0 + 1
    }

    @Test
    @DisplayName("이미 수락된 게시글 재수락 방지 테스트: 두 번째 수락 요청은 에러를 반환하고 포인트가 중복 지급되어서는 안 된다.")
    void alreadyAcceptedPostTest() throws Exception {
        // given
        saveTestUser(testUid, "게시글작성자");
        String acceptorUid = "acceptor_" + UUID.randomUUID();
        saveTestUser(acceptorUid, "수락자");
        String postId = createPost(testUid);

        String acceptRequestBody = """
                {
                    "acceptingUserId": "%s"
                }
                """.formatted(acceptorUid);

        // 첫 번째 수락 - 성공해야 함
        mockMvc.perform(
                        post("/api/posts/" + postId + "/accept")
                                .header("Authorization", "Bearer " + realJwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(acceptRequestBody))
                .andExpect(status().isOk());

        // when - 두 번째 수락 시도
        mockMvc.perform(
                        post("/api/posts/" + postId + "/accept")
                                .header("Authorization", "Bearer " + realJwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(acceptRequestBody))
                .andExpect(status().isBadRequest()); // 이미 수락됨 → 400 Bad Request

        // then - 포인트와 acceptCount는 첫 번째 수락분(+10, +1)만 반영되어야 함
        Optional<UserEntity> updatedAcceptor = userRepository.findById(acceptorUid);
        assertThat(updatedAcceptor).isPresent();
        assertThat(updatedAcceptor.get().getPoint()).isEqualTo(1010);   // 중복 지급 없이 1010 유지
        assertThat(updatedAcceptor.get().getAcceptCount()).isEqualTo(1); // 1번만
    }

    @Test
    @DisplayName("게시글 수락 동시성 테스트: 여러 요청이 동시에 들어와도 단 한 명만 수락에 성공해야 한다.")
    void acceptPostConcurrencyTest() throws Exception {
        // given
        saveTestUser(testUid, "게시글작성자");

        // 10명의 수락 시도자 생성
        int concurrentCount = 10;
        List<String> acceptorUids = new ArrayList<>();
        for (int i = 0; i < concurrentCount; i++) {
            String uid = "concurrent_acceptor_" + i + "_" + UUID.randomUUID();
            saveTestUser(uid, "수락시도자" + i);
            acceptorUids.add(uid);
        }

        String postId = createPost(testUid);

        // when - 10명이 동시에 수락 요청
        ExecutorService executor = Executors.newFixedThreadPool(concurrentCount);
        CountDownLatch latch = new CountDownLatch(concurrentCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (String acceptorUid : acceptorUids) {
            executor.submit(() -> {
                try {
                    String acceptBody = """
                            {
                                "acceptingUserId": "%s"
                            }
                            """.formatted(acceptorUid);

                    int httpStatus = mockMvc.perform(
                                    post("/api/posts/" + postId + "/accept")
                                            .header("Authorization", "Bearer " + realJwtToken)
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(acceptBody))
                            .andReturn().getResponse().getStatus();

                    if (httpStatus == 200) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    System.err.println("동시성 테스트 요청 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드 완료 대기
        executor.shutdown();

        // then - 단 1명만 수락에 성공해야 함
        assertThat(successCount.get()).isEqualTo(1);

        // Firestore에서도 단 하나의 수락만 처리되었는지 확인
        var postSnapshot = userRepository.getDb().collection("posts").document(postId).get().get();
        assertThat(postSnapshot.getBoolean("isAccept")).isTrue();
        assertThat(postSnapshot.getString("accepted_userId")).isNotNull();

        // 전체 수락자들 중 포인트가 올라간 사람이 정확히 1명인지 확인
        int rewardedCount = 0;
        for (String uid : acceptorUids) {
            Optional<UserEntity> acceptor = userRepository.findById(uid);
            if (acceptor.isPresent() && acceptor.get().getPoint() > 1000) {
                rewardedCount++;
            }
        }
        assertThat(rewardedCount).isEqualTo(1);
    }
}
