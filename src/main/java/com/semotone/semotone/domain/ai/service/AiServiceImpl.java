package com.semotone.semotone.domain.ai.service;

import com.semotone.semotone.domain.ai.dto.AiReqDto;
import com.semotone.semotone.domain.ai.dto.AiResDto;
import com.semotone.semotone.domain.ai.dto.AiResultResDto;
import com.semotone.semotone.domain.ai.entity.AiResultEntity;
import com.semotone.semotone.domain.ai.repository.aiRepository;
import com.semotone.semotone.domain.post.entity.PostEntity;
import com.semotone.semotone.domain.post.repository.PostRepository;
import com.semotone.semotone.util.GeminiClient;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class AiServiceImpl implements AiService {

    private final aiRepository aiRepository;
    private final PostRepository postRepository;
    private final GeminiClient geminiClient;

    public AiServiceImpl(aiRepository aiRepository, PostRepository postRepository, GeminiClient geminiClient) {
        this.aiRepository = aiRepository;
        this.postRepository = postRepository;
        this.geminiClient = geminiClient;
    }

    /**
     * 게시글 본문을 제미나이 API로 분석하여 AiResDto 반환
     *
     * 현재: 동기 방식 (응답 올 때까지 블로킹)
     * 비동기 전환 시:
     *   - 반환 타입을 CompletableFuture<AiResDto>로 변경
     *   - @Async 애노테이션 추가 및 AsyncConfig 설정
     *   - 또는 PostService에서 메시지 큐(RabbitMQ, Kafka)로 비동기 처리
     */
    @Override
    public AiResDto analyzePostText(AiReqDto aiReqDto) {
        return geminiClient.analyze(aiReqDto.getText());
    }

    /**
     * AI 분석 결과를 Firestore에 저장하고 AiResultResDto 반환
     * PostRepository에서 latitude, longitude 조회 후 함께 저장
     */
    @Override
    public AiResultResDto saveAiResult(String postId, AiResDto aiResDto) {
        // 게시글에서 위치 정보 조회
        PostEntity post;
        try {
            post = postRepository.findById(postId);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("AI 결과 저장 중 게시글 조회 실패: " + e.getMessage(), e);
        }

        if (post == null) {
            throw new RuntimeException("AI 결과 저장 대상 게시글을 찾을 수 없습니다: " + postId);
        }

        // AiResDto + Post 위치 정보 → AiResultEntity 생성
        AiResultEntity entity = AiResultEntity.builder()
                .postId(postId)
                .category(aiResDto.getCategory())
                .object(aiResDto.getObject())
                .type(aiResDto.getType())
                .urgency(aiResDto.getUrgency())
                .fromLocation(aiResDto.getFromLocation())
                .toLocation(aiResDto.getToLocation())
                .postLatitude(post.getLatitude())
                .postLongitude(post.getLongitude())
                .tags(AiResultEntity.AiTags.builder()
                        .type(aiResDto.getTags().getType())
                        .category(aiResDto.getTags().getCategory())
                        .object(aiResDto.getTags().getObject())
                        .urgency(aiResDto.getTags().getUrgency())
                        .build())
                .build();

        aiRepository.saveAiReq(entity);

        return AiResultResDto.fromEntity(entity);
    }

    /**
     * postId로 AI 분석 결과 단건 조회
     * AI 분석이 아직 완료되지 않은 경우 null 반환 (컨트롤러에서 빈 객체 처리)
     */
    @Override
    public AiResultResDto getAiResult(String postId) {
        Optional<AiResultEntity> optional = aiRepository.findByPostId(postId);
        return optional.map(AiResultResDto::fromEntity).orElse(null);
    }

    @Override
    public List<AiResultResDto> getAllAiResult() {
        return aiRepository.findAllAiResult().stream().map(
                aiResult -> AiResultResDto.fromEntity(aiResult)
        ).collect(Collectors.toList());

    }


}
