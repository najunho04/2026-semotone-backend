package com.semotone.semotone.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

/**
 * .env 파일에서 환경변수를 로드하는 설정 클래스
 * 애플리케이션 시작 시 자동으로 .env 파일의 변수를 System 환경변수로 등록합니다.
 */
@Configuration
public class DotenvConfig {

    static {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing() // .env 파일이 없어도 무시
                .load();

        // .env 파일의 모든 환경변수를 System 프로퍼티로 등록
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });
    }
}
