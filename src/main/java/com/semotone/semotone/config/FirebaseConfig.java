package com.semotone.semotone.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @Bean
    public Firestore firestore() {
        try {
            // 프로젝트 내부(resources 폴더)에 구워진 json 파일을 그대로 읽어옴
            InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream("firebase-key.json");

            if (serviceAccount == null) {
                throw new RuntimeException("❌ firebase-key.json 파일을 클래스패스에서 찾을 수 없습니다.");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            // Firebase가 이미 초기화되어 있는지 확인 후 초기화 진행
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("🎉 [Firebase] Admin SDK 초기화 대성공! (Base64 파일 주입 방식)");
            }

            return FirestoreClient.getFirestore();

        } catch (Exception e) {
            System.out.println("❌ [Firebase 에러 진짜 원인]: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Firebase 초기화 중 에러 발생", e);
        }
    }
}