package com.semotone.semotone.config;

import com.semotone.semotone.domain.ai.dto.AiResDto;
import com.semotone.semotone.util.GeminiClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 통합 테스트용 Config
 * - Mockito를 사용하여 GeminiClient의 analyze() 메서드를 Mock합니다.
 * - 실제 제미나이 API를 호출하지 않고 고정된 응답을 반환합니다.
 * - @Primary를 사용하여 테스트 실행 시 이 빈이 우선순위를 가집니다.
 */
@TestConfiguration
public class TestConfig {

    @Bean
    @Primary // 테스트 환경에서 이 빈을 우선 사용
    public GeminiClient mockGeminiClient() {
        GeminiClient mockClient = mock(GeminiClient.class);

        // analyze() 메서드가 호출되면 고정된 Mock 응답을 반환
        when(mockClient.analyze(anyString()))
                .thenReturn(
                    AiResDto.builder()
                        .category("배달")
                        .object("책")
                        .type("물건전달")
                        .urgency("보통")
                        .fromLocation("library")
                        .toLocation("elecNinfo")
                        .tags(AiResDto.TagsDto.builder()
                                .category("배달")
                                .object("책")
                                .type("물건전달")
                                .urgency("보통")
                                .build())
                        .build()
                );

        return mockClient;
    }
}
