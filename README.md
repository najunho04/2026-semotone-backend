# 🚀 [프로젝트명] Backend API Server

Flutter 클라이언트와 통신하며 핵심 비즈니스 로직(포인트, 게시글 수락 등)과 보안(JWT 검증)을 담당하는 Spring Boot 서버입니다. 1주일 데드라인에 맞춘 빠른 릴리즈를 목표로 합니다.

## 🛠 Tech Stack
- **Language:** Java 21
- **Framework:** Spring Boot 3.5.13
- **Database:** Firebase Cloud Firestore (NoSQL)
- **Auth:** Firebase Authentication (JWT 기반)
- **Architecture:** MVC Pattern (Controller, Service, Repository, DTO, Config)

## 📌 핵심 아키텍처 (클라이언트 - 서버 역할 분리)
1. **게시글/수락/포인트 로직:** `Flutter` -> `Spring API` -> `Firestore` (보안 및 데이터 무결성 검증)
2. **채팅 (실시간 통신):** Spring은 채팅방 생성(권한 부여)만 담당, 이후 채팅은 `Flutter` <-> `Firestore` 직접 연결로 부하 최소화
3. **인증:** Flutter에서 구글 로그인 후 발급받은 `ID Token`을 Spring Security Filter에서 Admin SDK로 검증

## ⚙️ 로컬 실행 방법 (Getting Started)

> **⚠️ 주의:** 프로젝트를 실행하기 위해서는 Firebase 관리자 권한 키가 필요합니다. 이 키는 보안상 GitHub에 올라가 있지 않습니다.

1. 이 저장소를 로컬에 Clone 합니다.
2. 팀장에게 `firebase-admin.json` 파일을 메신저로 개별 요청하여 받습니다. 혹은 직접 Firebase 콘솔로 이동 후 프로젝트 "설정" -> "서비스 계정" 에서 "Admin SDK 구성 스니펫"을 java로 선택 후 비공개 키를 생성하세요.
3. 전달받은 파일을 `src/main/resources/` 경로 안에 넣습니다.
4. IDE(IntelliJ 등)에서 `YourProjectApplication.java`를 실행합니다.
5. 콘솔에 `Firebase Admin SDK 초기화 성공!` 메시지가 뜨면 세팅 완료입니다.