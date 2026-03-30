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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc; // 가짜 HTTP 요청을 만들어주는 스프링 테스트 도구

    @Autowired
    private UserRepository userRepository; // Firestore에 진짜로 저장됐는지 확인할 때 사용

    private String realJwtToken;
    private String testUid;

    @BeforeEach
    void setUp() throws Exception {
        // 1. 테스트 실행 전, 진짜 구글 서버에서 토큰을 발급받아옵니다.
        FirebaseAuthTokenProvider provider = new FirebaseAuthTokenProvider();
        realJwtToken = provider.getTestToken("test@example.com", "password123");

        // 2. 발급받은 토큰을 디코딩해서 이번 테스트에 사용될 유저의 진짜 UID를 알아냅니다.
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(realJwtToken);
        testUid = decodedToken.getUid();
    }

    // ★ 추가할 부분: 테스트가 하나 끝날 때마다 무조건 실행되는 청소부!
    @AfterEach
    void tearDown() {
        if (testUid != null) {
            try {
                // 테스트에 사용했던 진짜 UID 문서를 Firestore에서 쿨하게 삭제해 버립니다.
                userRepository.getDb().collection("users").document(testUid).delete().get();
                System.out.println("테스트 데이터 청소 완료! 삭제된 UID: " + testUid);
            } catch (Exception e) {
                System.err.println("테스트 데이터 청소 실패: " + e.getMessage());
            }
        }
    }

    @Test
    @DisplayName("회원가입 통합 테스트: 실제 토큰 검증부터 Firestore 저장까지 완벽하게 동작해야 한다.")
    void signUpIntegrationTest() throws Exception {
        // given (준비)
        // 클라이언트가 보낼 JSON 데이터 (UserCreateReqDto 형태)
        String requestBody = """
                {
                    "nickName": "테스트유저1",
                    "latitude": 37.2479,
                    "longitude": 127.0770
                }
                """;

        // when (실행)
        // MockMvc를 사용해 "/api/users"로 POST 요청을 보냅니다. (헤더에 토큰 포함!)
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + realJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk()); // HTTP 상태 코드가 200(OK)인지 확인 (Controller 로직 통과 여부)

        // then (검증)
        // JwtFilter를 통과하고 UserService를 거쳐 UserRepositoryImpl이 Firestore에 데이터를 잘 넣었는지 확인!
        Optional<UserEntity> savedUser = userRepository.findById(testUid);

        assertThat(savedUser).isPresent(); // DB에 데이터가 존재해야 함
        assertThat(savedUser.get().getNickName()).isEqualTo("테스트유저1"); // 이름 확인
        assertThat(savedUser.get().getPoint()).isEqualTo(1000); // 신규 가입 포인트가 1000으로 세팅되었는지 확인
        assertThat(savedUser.get().getGmail()).isEqualTo("test@example.com"); // 토큰에서 뽑아낸 이메일이 잘 들어갔는지 확인
    }
}