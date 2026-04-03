package com.semotone.semotone.domain.ai.service;

import com.semotone.semotone.domain.ai.dto.AiResultResDto;
import com.semotone.semotone.domain.ai.entity.AiResultEntity;
import com.semotone.semotone.domain.ai.repository.aiRepository;
import com.semotone.semotone.domain.post.entity.PostEntity;
import com.semotone.semotone.domain.post.repository.PostRepository;
import com.semotone.semotone.domain.post.service.PostService;
import com.semotone.semotone.domain.post.service.PostServiceImpl;
import com.semotone.semotone.domain.user.entity.UserEntity;
import com.semotone.semotone.domain.user.repository.UserRepository;
import com.semotone.semotone.domain.user.service.UserService;
import com.semotone.semotone.domain.user.service.UserServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("AiService 통합 테스트")
class AiServiceTest {

    @Autowired
    private aiRepository aiRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private AiService aiService;
    private PostService postService;
    private UserService userService;
    private String postId1;
    private String postId2;
    private String userId;
    private List<String> createdAiResults = new ArrayList<>();

    @BeforeEach
    void setUp() throws ExecutionException, InterruptedException {
        // 테스트용 서비스 초기화
        aiService = new AiServiceImpl(aiRepository, postRepository, null);
        userService = new UserServiceImpl(userRepository);
        postService = new PostServiceImpl(postRepository, userService, aiService);

        // 테스트 데이터 생성
        userId = "user-" + UUID.randomUUID();
        postId1 = "post-" + UUID.randomUUID();
        postId2 = "post-" + UUID.randomUUID();

        // 1. 유저 생성
        UserEntity user = UserEntity.builder()
                .userId(userId)
                .nickName("테스트유저")
                .gmail(userId + "@test.com")
                .point(1000)
                .acceptCount(0)
                .latitude(37.5665)
                .longitude(126.9780)
                .build();
        userRepository.save(userId, user);

        // 2. 게시글 생성
        PostEntity post1 = PostEntity.builder()
                .userId(userId)
                .title("테스트 게시글 1")
                .content("테스트 콘텐츠 1")
                .deleted(false)
                .accepted(false)
                .completed(false)
                .rewardPoint(100)
                .latitude(37.5665)
                .longitude(126.9780)
                .build();
        postRepository.save(post1);

        PostEntity post2 = PostEntity.builder()
                .userId(userId)
                .title("테스트 게시글 2")
                .content("테스트 콘텐츠 2")
                .deleted(false)
                .accepted(false)
                .completed(false)
                .rewardPoint(150)
                .latitude(37.5666)
                .longitude(126.9781)
                .build();
        postRepository.save(post2);

        // 3. AI 분석 결과 생성 및 저장
        AiResultEntity aiResult1 = AiResultEntity.builder()
                .postId(postId1)
                .category("배달")
                .object("물건")
                .type("일반")
                .urgency("일반")
                .fromLocation("장소A")
                .toLocation("장소B")
                .postLatitude(37.5665)
                .postLongitude(126.9780)
                .tags(AiResultEntity.AiTags.builder()
                        .type("일반")
                        .category("배달")
                        .object("물건")
                        .urgency("일반")
                        .build())
                .build();
        aiRepository.saveAiReq(aiResult1);
        createdAiResults.add(postId1);

        AiResultEntity aiResult2 = AiResultEntity.builder()
                .postId(postId2)
                .category("심부름")
                .object("짐")
                .type("긴급")
                .urgency("높음")
                .fromLocation("장소C")
                .toLocation("장소D")
                .postLatitude(37.5666)
                .postLongitude(126.9781)
                .tags(AiResultEntity.AiTags.builder()
                        .type("긴급")
                        .category("심부름")
                        .object("짐")
                        .urgency("높음")
                        .build())
                .build();
        aiRepository.saveAiReq(aiResult2);
        createdAiResults.add(postId2);
    }

    @AfterEach
    void tearDown() throws Exception {
        try {
            // AI 결과 삭제
            for (String postId : createdAiResults) {
                try {
                    userRepository.getDb().collection("ai_results").document(postId).delete().get();
                } catch (Exception e) {
                    // 이미 삭제되었을 수 있음
                }
            }

            // 게시물 삭제
            if (postId1 != null) {
                postRepository.delete(postId1);
            }
            if (postId2 != null) {
                postRepository.delete(postId2);
            }

            // 유저 삭제
            if (userId != null) {
                userRepository.getDb().collection("users").document(userId).delete().get();
            }
        } catch (Exception e) {
            System.err.println("Cleanup failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("getAllAiResult - 모든 AI 분석 결과를 조회하여 리스트로 반환")
    void testGetAllAiResult() throws Exception {
        // When: 모든 AI 결과 조회
        List<AiResultResDto> results = aiService.getAllAiResult();

        // Then: 1. 결과 리스트가 null이 아닌지 확인
        assertThat(results).isNotNull();
        System.out.println("✓ AI 결과 리스트 조회 성공, 전체 개수: " + results.size());

        // 2. 결과 리스트가 비어있지 않은지 확인 (생성한 2개 이상)
        assertThat(results.size()).isGreaterThanOrEqualTo(2);
        System.out.println("✓ 조회된 AI 결과 개수 >= 2");

        // 3. 각 결과의 구조가 올바른지 확인
        for (AiResultResDto result : results) {
            assertThat(result.getPostId()).isNotNull();
            assertThat(result.getCategory()).isNotNull();
            assertThat(result.getTags()).isNotNull();
            System.out.println("✓ AI 결과 구조 검증");
            System.out.println("  - postId: " + result.getPostId());
            System.out.println("  - category: " + result.getCategory());
            System.out.println("  - urgency: " + result.getUrgency());
        }

        // 4. 생성한 데이터가 포함되어 있는지 확인
        boolean foundPost1 = results.stream()
                .anyMatch(r -> r.getPostId().equals(postId1));
        boolean foundPost2 = results.stream()
                .anyMatch(r -> r.getPostId().equals(postId2));

        assertThat(foundPost1).isTrue();
        assertThat(foundPost2).isTrue();
        System.out.println("✓ 생성한 AI 결과 데이터 포함 확인");
    }

    @Test
    @DisplayName("getAllAiResult - 반환된 데이터의 세부 정보 검증")
    void testGetAllAiResultDataValidation() throws Exception {
        // When: 모든 AI 결과 조회
        List<AiResultResDto> results = aiService.getAllAiResult();

        // Then: 생성한 postId1의 결과 찾기
        AiResultResDto result1 = results.stream()
                .filter(r -> r.getPostId().equals(postId1))
                .findFirst()
                .orElse(null);

        assertThat(result1).isNotNull();
        System.out.println("✓ postId1 결과 조회 성공");

        // 1. postId1의 데이터 검증
        assertThat(result1.getCategory()).isEqualTo("배달");
        assertThat(result1.getObject()).isEqualTo("물건");
        assertThat(result1.getType()).isEqualTo("일반");
        assertThat(result1.getUrgency()).isEqualTo("일반");
        assertThat(result1.getFromLocation()).isEqualTo("장소A");
        assertThat(result1.getToLocation()).isEqualTo("장소B");
        System.out.println("✓ postId1 데이터 검증 완료");

        // 2. 위치 정보 검증
        assertThat(result1.getPostLatitude()).isEqualTo(37.5665);
        assertThat(result1.getPostLongitude()).isEqualTo(126.9780);
        System.out.println("✓ postId1 위치 정보 검증 완료");

        // 3. Tags 정보 검증
        assertThat(result1.getTags()).isNotNull();
        assertThat(result1.getTags().getType()).isEqualTo("일반");
        assertThat(result1.getTags().getCategory()).isEqualTo("배달");
        assertThat(result1.getTags().getObject()).isEqualTo("물건");
        System.out.println("✓ postId1 Tags 정보 검증 완료");

        // 4. postId2의 데이터 검증
        AiResultResDto result2 = results.stream()
                .filter(r -> r.getPostId().equals(postId2))
                .findFirst()
                .orElse(null);

        assertThat(result2).isNotNull();
        assertThat(result2.getCategory()).isEqualTo("심부름");
        assertThat(result2.getUrgency()).isEqualTo("높음");
        System.out.println("✓ postId2 데이터 검증 완료");
    }

    @Test
    @DisplayName("getAllAiResult - 많은 데이터 조회 성능 확인")
    void testGetAllAiResultPerformance() throws Exception {
        // When: 모든 AI 결과 조회
        long startTime = System.currentTimeMillis();
        List<AiResultResDto> results = aiService.getAllAiResult();
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;

        // Then: 1. 결과를 성공적으로 조회
        assertThat(results).isNotNull();
        System.out.println("✓ 모든 AI 결과 조회 완료");

        // 2. 조회 시간 로깅 (성능 확인용)
        System.out.println("✓ 조회 시간: " + duration + "ms");
        System.out.println("✓ 조회된 결과 개수: " + results.size());

        // 3. 각 결과가 DTO로 올바르게 변환되었는지 확인
        for (AiResultResDto result : results) {
            assertThat(result).isNotNull();
            assertThat(result.getPostId()).isNotBlank();
            // Stream.map()에서 fromEntity() 호출이 정상 작동하는지 확인
        }
        System.out.println("✓ 모든 결과가 DTO로 올바르게 변환됨");
    }
}
