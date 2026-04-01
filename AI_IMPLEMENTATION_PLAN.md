# AI 기능 개발 계획서

## 📋 전체 로직 흐름 (요약)
```
1. 클라이언트 → 게시물 생성 요청
2. 서버 → 게시물 저장 (PostEntity → Firestore)
3. 서버 → AI 분석 요청 (Post 내용 → AiReqDto → 제미나이 API)
4. 서버 → AI 분석 결과 저장 (AiResDto → AiResultEntity → Firestore)
5. 클라이언트 → 게시물 상세 조회 시 AI 결과도 함께 반환
```

---

## 🏗️ 아키텍처 구조

### 현재 상태
```
domain/ai/
├── dto/
│   ├── AiReqDto          ✅ (완성: text만 포함)
│   ├── AiResDto          ✅ (완성: category, object, type, urgency, fromLocation, toLocation, tags)
│   └── AiResultResDto    ✅ (완성: Entity→DTO 변환 메서드 포함)
├── entity/
│   └── AiResultEntity    ✅ (완성: postId as DocumentId, postLatitude, postLongitude, tags)
├── repository/
│   └── aiRepository      ✅ (인터페이스만: saveAiReq, findByPostId)
└── service/
    └── AiService         ✅ (인터페이스만: analyzePostText, saveAiResult, getAiResult)
```

### 필요한 추가 구현
- `AiRepositoryImpl` (Firestore 연동)
- `AiServiceImpl` (제미나이 API 호출 + 저장 + 조회)
- `PostService` 수정 (게시물 생성 후 AI 분석 연동)
- `PostController` 수정/추가 (AI 결과 조회 엔드포인트)

---

## 📌 상세 구현 계획

### Phase 1: Repository 구현 (`AiRepositoryImpl`)

#### 1-1. Firestore 연동 설정
- Firestore 클라이언트 주입 (`FirebaseConfig`에서 제공)
- Collection 이름: `ai_results` (postId를 문서 ID로 사용)

#### 1-2. `saveAiReq(AiResultEntity aiResult)` 메서드
```
목적: AI 분석 결과를 Firestore에 저장
로직:
  1. AiResultEntity를 받음
  2. Firestore의 "ai_results" 컬렉션에 저장
  3. Document ID = postId (1:1 매칭)
  4. postId가 이미 존재하면 덮어쓰기 (수정 시나리오)
에러처리: Firestore 저장 실패 시 예외 처리
```

#### 1-3. `findByPostId(String postId)` 메서드
```
목적: 특정 게시물의 AI 분석 결과 조회
로직:
  1. postId로 문서 조회
  2. 존재하면 AiResultEntity로 파싱
  3. Optional<AiResultEntity> 반환
에러처리: 데이터 없으면 Optional.empty() 반환
```

---

### Phase 2: Service 구현 (`AiServiceImpl`)

#### 2-1. `analyzePostText(AiReqDto aiReqDto)` 메서드
```
목적: 제미나이 API를 호출하여 텍스트 분석
로직:
  1. AiReqDto (text)를 제미나이 API에 전송
  2. 응답을 파싱하여 AiResDto 구조로 변환
  3. category, object, type, urgency, fromLocation, toLocation, tags 추출
  4. AiResDto 반환

필요사항:
  - GeminiClient를 SpringConfig에서 빈으로 등록
  - API 키: "INPUT-GEMINI-API-KEY" (임시, 실제 값으로 교체)
  - API 호출 타임아웃 설정
  - 응답 파싱 로직 (JSON → AiResDto)
  - GeminiClient 의존성 주입

에러처리:
  - API 호출 실패 (timeout, network error) → 예외 발생
  - 응답 파싱 실패 → 예외 발생
  - 예외 발생 시 최상위에서 처리하도록 위임
```

#### 2-2. `saveAiResult(String postId, AiResDto aiResDto)` 메서드
```
목적: AI 분석 결과를 Firestore에 저장하고 응답용 DTO로 변환
로직:
  1. AiResDto와 postId를 받음
  2. Post 정보에서 latitude, longitude 조회 (→ postLatitude, postLongitude)
  3. AiResultEntity 객체 생성
     - postId, category, object, type, urgency, fromLocation, toLocation, tags
     - postLatitude, postLongitude (PostRepository에서 조회 필요)
  4. aiRepository.saveAiReq(entity) 호출하여 저장
  5. AiResultResDto.fromEntity() 호출하여 변환 후 반환

의존성:
  - PostRepository (latitude, longitude 조회용)
  - aiRepository (저장용)

에러처리:
  - Post 조회 실패 → 예외 처리
  - Firestore 저장 실패 → 예외 처리
```

#### 2-3. `getAiResult(String postId)` 메서드
```
목적: 게시물의 AI 분석 결과 조회
로직:
  1. postId로 조회 (aiRepository.findByPostId)
  2. Optional<AiResultEntity>이 비어있으면 null 반환 (또는 예외)
  3. AiResultResDto.fromEntity()로 변환하여 반환

에러처리:
  - 데이터 없으면 null 또는 RuntimeException 발생
```

---

### Phase 3: PostService 수정 (`PostServiceImpl`)

#### 3-1. `createPost()` 메서드 수정
```
현재 흐름:
  1. PostEntity 생성
  2. postRepository.save(post) → postId 반환
  3. userService.addPostIdToUser(userId, postId) 호출
  4. postId 반환

수정 흐름:
  1. PostEntity 생성 및 저장 (위와 동일)
  2. userService.addPostIdToUser(userId, postId) 호출 (위와 동일)
  3. ✨ NEW: AI 분석 요청
     - PostEntity.content를 AiReqDto로 변환
     - aiService.analyzePostText(aiReqDto) 호출 → AiResDto 받음
     - aiService.saveAiResult(postId, aiResDto) 호출 → 저장
  4. postId 반환

설계 결정사항:
  ✅ **동기 방식 처리**
  - AI 분석이 완료될 때까지 클라이언트 대기
  - 구현 단순, 결과 즉시 반영
  
  💡 비동기로 전환하려면:
  - @Async 애노테이션 추가 또는
  - 스케줄러(Scheduler) / 메시지 큐(RabbitMQ, Kafka) 도입
  - 게시물 생성 후 즉시 반환, 분석은 백그라운드에서 진행
  - PostEntity에 aiStatus 필드 추가 (PENDING, COMPLETED, FAILED)

에러처리:
  ✅ **AI 분석 실패 시 예외 발생**
  - 게시물 생성 실패로 처리
  - 클라이언트에 에러 메시지 전달
  - PostEntity는 저장되지 않음 (롤백)
```

---

### Phase 4: PostController 수정/추가

#### 4-1. `createPost()` - 에러 처리 확장
```
현재:
  - ExecutionException, InterruptedException 처리

수정:
  - AI 분석 실패 예외 추가 처리
  - 제미나이 API 타임아웃 예외 처리
  - 적절한 HTTP 상태 코드 반환
```

#### 4-2. AI 결과 조회 엔드포인트 추가
```
엔드포인트: GET /posts/{postId}/ai
목적: 특정 게시물의 AI 분석 결과 조회

로직:
  1. aiService.getAiResult(postId) 호출
  2. AiResultResDto 반환

응답 예시:
{
  "postId": "post123",
  "category": "배달",
  "object": "책",
  "type": "물건전달",
  "urgency": "보통",
  "fromLocation": "library",
  "toLocation": "elecNinfo",
  "postLatitude": 37.2977,
  "postLongitude": 127.0082,
  "tags": {
    "type": "물건전달",
    "category": "배달",
    "object": "책",
    "urgency": "보통"
  }
}

에러처리:
  - postId 존재하지 않음 → 404 Not Found
  - AI 분석 결과 없음 → ✅ **빈 객체 반환** (200 OK)
    - 서버 에러 아님, 클라이언트에서 나중에 재요청 가능
    - AiResultResDto의 모든 필드가 null/0으로 채워짐
```

#### 4-3. ✅ 게시물 상세 조회는 기존대로 유지
```
엔드포인트: GET /posts/{postId}
동작: PostResDto만 반환 (AI 결과 미포함)

이유:
- 클라이언트가 AI 결과 필요 시만 별도 요청 (GET /posts/{postId}/ai)
- REST 설계 원칙 준수
- 불필요한 데이터 전송 방지
```

---

## 🔄 의존성 및 주입

```
PostServiceImpl
  ├── PostRepository
  ├── UserService
  └── AiService (✨ 새로 추가)
      ├── aiRepository
      └── PostRepository (latitude, longitude 조회용)

PostController
  └── PostService

AiServiceImpl
  ├── aiRepository
  ├── PostRepository
  └── GeminiClient (✨ 새로 추가)

GeminiClient
  └── API 키: "INPUT-GEMINI-API-KEY"
```

---

## ⚙️ SpringConfig 수정사항

### 새로 추가해야 할 빈 등록

```java
// 1. GeminiClient 빈 등록
@Bean
public GeminiClient geminiClient() {
    String apiKey = "INPUT-GEMINI-API-KEY"; // 임시 키, 실제 값으로 교체
    return new GeminiClient(apiKey);
}

// 2. AiRepository 빈 등록
@Bean
public aiRepository aiRepository(Firestore firestore) {
    return new AiRepositoryImpl(firestore);
}

// 3. AiService 빈 등록
@Bean
public AiService aiService(aiRepository aiRepository, 
                           PostRepository postRepository,
                           GeminiClient geminiClient) {
    return new AiServiceImpl(aiRepository, postRepository, geminiClient);
}
```

### 수정 위치
- 파일: `src/main/java/com/semotone/semotone/config/SpringConfig.java`
- 위치: 기존 @Bean 메서드 다음에 추가
- Firestore 빈이 이미 등록되어 있으므로 주입받아 사용

---

## 🧪 테스트 고려사항

### Unit Test
- `AiServiceImpl.analyzePostText()`: 제미나이 API 모킹
- `AiServiceImpl.saveAiResult()`: Firestore 모킹
- `AiRepositoryImpl`: Firestore 통합 테스트

### Integration Test
- `PostService.createPost()`: 게시물 생성 후 AI 분석까지 전체 흐름

---

## ⚠️ 주의사항 & 결정 사항 (최종)

| 항목 | 결정 | 비고 |
|------|------|------|
| 동기 vs 비동기 | ✅ **동기 방식** | 주석으로 비동기 전환 가능성 표시 |
| AI 분석 실패 시 처리 | ✅ **예외 발생** | 게시물 생성 실패로 처리, 클라이언트에 오류 전달 |
| 게시물 상세 조회에 AI 결과 포함 | ✅ **별도 엔드포인트** | GET /posts/{postId}/ai로 독립적 호출 |
| AI 결과 미존재 시 응답 | ✅ **빈 객체 반환** | 404 아님, 클라이언트가 필요 시 재요청 가능 |
| 제미나이 API 키 | ✅ **"INPUT-GEMINI-API-KEY"** | 실제 API 키 받은 후 교체 |
| 빈 등록 위치 | ✅ **SpringConfig** | 모든 빈 등록을 config/SpringConfig에서 관리 |

---

## 📅 구현 순서

```
1단계: AiRepositoryImpl 구현 (Firestore 저장/조회)
2단계: AiServiceImpl 구현 (API 호출, 저장, 조회)
3단계: PostServiceImpl 수정 (게시물 생성 시 AI 분석 연동)
4단계: PostController 수정/추가 (에러 처리, AI 조회 엔드포인트)
5단계: 통합 테스트
```

---

## 📝 구현 파일 체크리스트

### 필수 구현
- [ ] `src/main/java/com/semotone/semotone/util/GeminiClient.java` (new) - 제미나이 API 호출
- [ ] `src/main/java/com/semotone/semotone/domain/ai/repository/AiRepositoryImpl.java` (new)
- [ ] `src/main/java/com/semotone/semotone/domain/ai/service/AiServiceImpl.java` (new)
- [ ] `src/main/java/com/semotone/semotone/domain/post/service/PostServiceImpl.java` (modify)
- [ ] `src/main/java/com/semotone/semotone/domain/post/controller/PostController.java` (modify)
- [ ] `src/main/java/com/semotone/semotone/config/SpringConfig.java` (modify) - 빈 등록

### 선택사항
- [ ] 테스트 코드
- [ ] 통합 테스트
