package com.semotone.semotone.auth;

import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class FirebaseAuthTokenProvider {
    // Firebase Console -> 프로젝트 설정 -> 일반 -> Web API 키
    private static final String API_KEY = "input_your_Web_api_key";
    private static final String AUTH_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + API_KEY;

    public String getTestToken(String email, String password) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> request = Map.of(
                "email", email,
                "password", password,
                "returnSecureToken", true
        );

        // 구글 서버에 로그인 요청을 보내고 응답을 받습니다.
        Map<String, Object> response = restTemplate.postForObject(AUTH_URL, request, Map.class);

        if (response == null || !response.containsKey("idToken")) {
            throw new RuntimeException("토큰 발급 실패! 이메일/비밀번호나 API 키를 확인해주세요.");
        }

        return (String) response.get("idToken"); // 진짜 JWT 반환!
    }
}