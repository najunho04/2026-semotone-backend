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

    @Bean // 이 어노테이션이 있어야 스프링이 'Firestore'를 관리해줘.
    public Firestore firestore() {
        try {
            InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream("firebase-key.json");

            if (serviceAccount == null) {
                throw new RuntimeException("firebase-key.json 파일을 찾을 수 없습니다.");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase Admin SDK 초기화 성공!");
            }

            // 초기화된 FirebaseApp에서 Firestore 객체를 가져와 빈으로 등록!
            return FirestoreClient.getFirestore();

        } catch (Exception e) {
            throw new RuntimeException("Firebase 초기화 중 에러 발생", e);
        }
    }
}