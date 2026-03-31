package com.semotone.semotone.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
            // src/main/resources/ 에 있는 키 파일을 읽어옵니다.
            InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream("firebase-key.json");

            if (serviceAccount == null) {
                throw new RuntimeException("firebase-key.json 파일을 찾을 수 없습니다.");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            // 이미 초기화되어 있는지 확인 후 초기화
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase Admin SDK 초기화 성공!");
            }
        } catch (Exception e) {
            System.out.println("Firebase Admin SDK 초기화 Error 발생!: " + e);
        }
    }

    @Bean
    public Firestore firestore() throws IOException {
        // 서비스 계정 키 파일 경로 (resources 폴더 등에 위치)
        FileInputStream serviceAccount =
                new FileInputStream("src/main/resources/serviceAccountKey.json");

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        if (FirebaseApp.getApps().isEmpty()) { // 중복 초기화 방지
            FirebaseApp.initializeApp(options);
        }

        return FirestoreClient.getFirestore();
    }
}