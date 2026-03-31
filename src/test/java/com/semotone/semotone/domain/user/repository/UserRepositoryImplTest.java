package com.semotone.semotone.domain.user.repository;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.semotone.semotone.domain.user.entity.UserEntity;
import org.junit.jupiter.api.*;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS) // BeforeAll을 static 없이 쓰기 위함
class UserRepositoryImplTest {

    private UserRepository userRepository;

    @BeforeAll
    void setup() throws IOException {
        // 테스트 실행 전 Firebase 초기화 (실제 DB에 연결되므로 주의!)
        InputStream serviceAccount = new ClassPathResource("firebase-admin.json").getInputStream();

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }

        userRepository = new UserRepositoryImpl();
    }

    @Test
    @DisplayName("유저 저장 및 조회 테스트")
    void saveAndFindTest() {
        // given (준비)
        String testUid = "test_user_123";
        UserEntity user = UserEntity.builder()
                .nickName("테스터")
                .gmail("test@gmail.com")
                .point(1000)
                .build();

        // when (실행)
        userRepository.save(testUid, user);
        Optional<UserEntity> foundUser = userRepository.findById(testUid);

        // then (검증)
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getNickName()).isEqualTo("테스터");
        assertThat(foundUser.get().getPoint()).isEqualTo(1000);
    }

    @Test
    @DisplayName("포인트 증감(increment) 동시성 방어 테스트")
    void incrementPointTest() {
        // given
        String testUid = "test_user_123";

        // when
        userRepository.addPointAndAcceptCount(testUid, 50, 1);
        Optional<UserEntity> updatedUser = userRepository.findById(testUid);

        // then
        // 기존 1000점에서 50점이 더해져 1050점이 되었는지 확인
        assertThat(updatedUser.get().getPoint()).isEqualTo(1050);
        assertThat(updatedUser.get().getAcceptCount()).isEqualTo(1);
    }
}