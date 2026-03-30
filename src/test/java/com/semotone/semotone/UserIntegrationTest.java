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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
        mockMvc.perform( // 1. "자, 가짜 포스트맨! 지금부터 내가 설정한 대로 전송(Send) 버튼을 눌러!"
                        post("/api/users") // 2. "방식은 POST이고, 목적지 주소는 /api/users 야."
                                .header("Authorization", "Bearer " + realJwtToken) // 3. "헤더(Header)에 신분증(JWT 토큰)을 딱 붙여서 보내."
                                .contentType(MediaType.APPLICATION_JSON) // 4. "내가 보낼 데이터의 포맷은 JSON 형식이야."
                                .content(requestBody) // 5. "그리고 아까 위(given)에서 만들어둔 JSON 문자열(닉네임, 위도, 경도)을 편지 봉투(Body) 안에 쏙 넣어."
                )
                .andExpect(status().isOk()); // 6. "요청을 보냈을 때, 서버가 곧바로 200(OK) 성공 상태 코드를 뱉어내는지 확인해!"

        // then (검증)
        // JwtFilter를 통과하고 UserService를 거쳐 UserRepositoryImpl이 Firestore에 데이터를 잘 넣었는지 확인!
        Optional<UserEntity> savedUser = userRepository.findById(testUid);

        assertThat(savedUser).isPresent(); // DB에 데이터가 존재해야 함
        assertThat(savedUser.get().getNickName()).isEqualTo("테스트유저1"); // 이름 확인
        assertThat(savedUser.get().getPoint()).isEqualTo(1000); // 신규 가입 포인트가 1000으로 세팅되었는지 확인
        assertThat(savedUser.get().getGmail()).isEqualTo("test@example.com"); // 토큰에서 뽑아낸 이메일이 잘 들어갔는지 확인
    }

    @Test
    @DisplayName("위치 업데이트 통합 테스트: PATCH 요청 시 DB의 위도/경도만 정상적으로 변경되어야 한다.")
    void updateLocationIntegrationTest() throws Exception {
        // given (준비)
        // 1. 위치를 업데이트하려면 DB에 유저가 먼저 존재해야 합니다. (Firestore의 update는 문서가 없으면 에러가 납니다)
        UserEntity initialUser = UserEntity.builder()
                .nickName("위치테스터")
                .gmail("test@example.com")
                .point(1000)
                .acceptCount(0)
                // 초기 위치는 0.0 으로 세팅
                .latitude(0.0)
                .longitude(0.0)
                .build();
        userRepository.save(testUid, initialUser); // 테스트용 초기 데이터 강제 주입

        // 2. 클라이언트(Flutter)가 20초마다 보낼 새로운 위치 JSON 데이터
        // (예: 경기도 용인시 근처 좌표)
        String locationUpdateJson = """
                {
                    "latitude": 37.2479,
                    "longitude": 127.0770
                }
                """;

        // when (실행)
        // MockMvc로 PATCH 요청을 보냅니다.
        mockMvc.perform(patch("/api/users/location") // POST가 아니라 PATCH입니다!
                        .header("Authorization", "Bearer " + realJwtToken) // 진짜 토큰 장착
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(locationUpdateJson))
                .andExpect(status().isOk()); // HTTP 200 OK 응답 확인

        // then (검증)
        // DB에서 해당 유저를 다시 꺼내서 위치가 정말 바뀌었는지 확인합니다.
        Optional<UserEntity> updatedUser = userRepository.findById(testUid);

        assertThat(updatedUser).isPresent();
        // 위치가 초기값 0.0에서 37.2479, 127.0770으로 잘 덮어씌워졌는지 검증!
        assertThat(updatedUser.get().getLatitude()).isEqualTo(37.2479);
        assertThat(updatedUser.get().getLongitude()).isEqualTo(127.0770);

        // 주의: 다른 필드(포인트 등)는 건드리지 않았으므로 그대로 유지되어야 합니다.
        assertThat(updatedUser.get().getPoint()).isEqualTo(1000);
    }

    @Test
    @DisplayName("내 정보 조회 통합 테스트: GET 요청 시 내 프로필 정보를 정상적으로 반환해야 한다.")
    void getMyProfileIntegrationTest() throws Exception {
        // given (준비)
        // 조회를 하려면 DB에 유저가 먼저 있어야 하므로 임의로 한 명 저장해 둡니다.
        UserEntity initialUser = UserEntity.builder()
                .nickName("조회테스터")
                .gmail("test@example.com")
                .point(5000) // 5000 포인트 세팅!
                .acceptCount(3)
                .build();
        userRepository.save(testUid, initialUser);

        // when (실행) & then (검증)
        // GET 방식은 보낼 데이터(Body)가 없으므로 아주 심플합니다.
        mockMvc.perform(get("/api/users/me") // GET 요청!
                        .header("Authorization", "Bearer " + realJwtToken)) // 토큰만 달랑 들고 갑니다.
                .andExpect(status().isOk()) // 1. 통신이 200 OK로 성공했는가?
                // 2. 날아온 JSON 응답 데이터($.필드명)가 내가 방금 넣은 값과 일치하는가?
                .andExpect(jsonPath("$.nickName").value("조회테스터"))
                .andExpect(jsonPath("$.point").value(5000))
                .andExpect(jsonPath("$.acceptCount").value(3))
                .andExpect(jsonPath("$.gmail").value("test@example.com"));
    }

    @Test
    @DisplayName("포인트 및 채택 수 증가 통합 테스트: 서비스 로직 실행 시 Firestore에 값들이 정상적으로 누적되어야 한다.")
    void addPointAndAcceptCountIntegrationTest() throws Exception {
        // given (준비)
        // 테스트 전용 유저를 Firestore에 미리 저장해 둡니다.
        UserEntity initialUser = UserEntity.builder()
                .nickName("포인트테스터")
                .gmail("test@example.com")
                .point(1000)      // 초기 포인트 1000
                .acceptCount(0)   // 초기 채택 수 0
                .build();
        userRepository.save(testUid, initialUser);

        int pointToAdd = 50; // 이번에 획득할 포인트

        // when (실행)
        // API(MockMvc)를 통하지 않고, 방금 만든 서비스 로직을 직접 실행합니다!
        // (메서드 파라미터가 uid와 증가시킬 포인트라고 가정했습니다. 실제 시그니처에 맞게 수정해주세요)
        userRepository.addPointAndAcceptCount(testUid, pointToAdd, 1);

        // then (검증)
        // 서비스 로직이 끝난 후, Firestore에서 진짜 유저 문서를 꺼내와서 값이 잘 더해졌는지 확인합니다.
        Optional<UserEntity> updatedUser = userRepository.findById(testUid);

        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getPoint()).isEqualTo(1050); // 1000 + 50 = 1050이 되었는지 확인!
        assertThat(updatedUser.get().getAcceptCount()).isEqualTo(1); // 0 + 1 = 1번이 되었는지 확인!
    }

    @Test
    @DisplayName("게시글 ID 추가 통합 테스트: 서비스 로직 실행 시 유저의 작성 글 목록에 새로운 ID가 정상적으로 들어가야 한다.")
    void addPostIdToUserIntegrationTest() throws Exception {
        // given (준비)
        UserEntity initialUser = UserEntity.builder()
                .nickName("게시글테스터")
                .gmail("test@example.com")
                .point(1000)      // 초기 포인트 1000
                .acceptCount(0)   // 초기 채택 수 0
                // UserEntity 내부에 List<Long> 또는 List<String> 형태의 postIds 필드가 있다고 가정합니다.
                .build();
        userRepository.save(testUid, initialUser);

        // Firestore 게시글 ID 형태(String) 또는 Long 등 사용하시는 타입에 맞춰주세요.
        String newPostId = "testPostId";

        // when (실행)
        // 서비스 로직을 호출하여 유저 정보에 방금 쓴 게시글 ID를 밀어 넣습니다.
        userRepository.addPostIdToUser(testUid, newPostId);

        // then (검증)
        // DB에서 최신 유저 정보를 가져옵니다.
        Optional<UserEntity> updatedUser = userRepository.findById(testUid);

        assertThat(updatedUser).isPresent();
        // UserEntity에 선언하신 리스트 필드명(예: getPostIds())으로 검증합니다.
        // 리스트 안에 방금 넣은 999L이 예쁘게 들어있는지 확인!
        assertThat(updatedUser.get().getMyPosts()).contains(newPostId);
    }
}