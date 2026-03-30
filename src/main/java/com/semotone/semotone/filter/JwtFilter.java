package com.semotone.semotone.filter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 클라이언트의 요청 헤더에서 "Authorization" 값을 가져옵니다.
        String header = request.getHeader("Authorization");

        // 2. 토큰이 없거나 "Bearer "로 시작하지 않으면 검증을 포기하고 다음 필터로 넘깁니다. (비로그인 상태)
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. "Bearer " 글자를 떼어내고 순수 JWT 토큰만 추출합니다.
        String token = header.substring(7);

        try {
            // 4. Firebase Admin SDK를 사용해 토큰이 진짜인지(유효기간, 위조 여부) 검사합니다.
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);

            // 5. 검증이 통과되면 토큰 안에서 유저의 고유 UID와 Email을 꺼냅니다.
            String uid = decodedToken.getUid();
            String email = decodedToken.getEmail(); // 토큰 안에 이메일도 들어있습니다!

            // 6. Spring Security에게 "이 요청은 이 UID를 가진 유저가 보낸 게 확실해!"라고 알려줍니다.
            // 이렇게 해두면 나중에 Controller에서 이 UID를 편하게 꺼내 쓸 수 있습니다.
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(uid, email, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("인증 성공! 접속한 유저 UID: {}, Email: {}", uid, email);

        } catch (Exception e) {
            // 토큰이 만료되었거나 조작된 경우 에러를 냅니다.
            log.error("Firebase 토큰 검증 실패: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("INVALID_TOKEN");
            return;
        }

        // 검증이 무사히 끝났으니 원래 가려던 Controller로 요청을 통과시켜 줍니다.
        filterChain.doFilter(request, response);
    }
}