package com.semotone.semotone.domain.chat.service;

import com.google.cloud.firestore.Firestore;
import com.semotone.semotone.domain.chat.repository.ChatRoomRepositoryImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("ChatRoomService 통합 테스트")
class ChatRoomServiceTest {

    @Autowired
    private Firestore firestore;

    private ChatRoomService chatRoomService;
    private String createdRoomId;
    private String posterId;
    private String accepterId;

    @BeforeEach
    void setUp() {
        // 테스트용 서비스 초기화
        ChatRoomRepositoryImpl chatRoomRepository = new ChatRoomRepositoryImpl(firestore);
        chatRoomService = new ChatRoomServiceImpl(chatRoomRepository);

        // 테스트 데이터 생성
        posterId = "poster-" + UUID.randomUUID();
        accepterId = "accepter-" + UUID.randomUUID();
    }

    @AfterEach
    void tearDown() throws Exception {
        // 생성된 채팅방 삭제
        if (createdRoomId != null) {
            firestore.collection("chats").document(createdRoomId).delete().get();
        }
    }

    @Test
    @DisplayName("채팅방 생성 - posterId와 accepterId를 받아 새로운 채팅방 생성")
    void testCreateChatRoom() throws Exception {
        // When: 채팅방 생성
        createdRoomId = chatRoomService.createOrGetChatRoom(posterId, accepterId);

        // Then: 1. 방 ID가 정상적으로 반환되었는지 확인
        assertThat(createdRoomId).isNotNull();
        assertThat(createdRoomId).isNotEmpty();
        System.out.println("✓ 채팅방 생성 완료, roomId: " + createdRoomId);

        // 2. Firestore에 chats 컬렉션에 데이터가 저장되었는지 확인
        var roomDoc = firestore.collection("chats").document(createdRoomId).get().get();
        assertThat(roomDoc.exists()).isTrue();
        System.out.println("✓ Firestore chats 컬렉션에 데이터 저장 확인");

        // 3. 저장된 데이터 내용 검증
        assertThat(roomDoc.get("requesterId")).isEqualTo(posterId);
        assertThat(roomDoc.get("helperId")).isEqualTo(accepterId);
        assertThat(roomDoc.get("lastMessage")).isEqualTo("");
        System.out.println("✓ 저장된 데이터 내용 검증");
        System.out.println("  - requesterId: " + roomDoc.get("requesterId"));
        System.out.println("  - helperId: " + roomDoc.get("helperId"));
        System.out.println("  - lastMessage: " + roomDoc.get("lastMessage"));

        // 4. users 배열에 두 유저가 포함되어 있는지 확인
        @SuppressWarnings("unchecked")
        var users = (java.util.List<String>) roomDoc.get("users");
        assertThat(users).contains(posterId, accepterId);
        System.out.println("✓ users 배열에 두 유저 포함 확인");

        // 5. lastMessageAt가 설정되었는지 확인
        assertThat(roomDoc.get("lastMessageAt")).isNotNull();
        System.out.println("✓ lastMessageAt 타임스탬프 설정 확인");
    }

    @Test
    @DisplayName("여러 채팅방 생성 - 다중 채팅방이 서로 독립적으로 생성되는지 확인")
    void testCreateMultipleChatRooms() throws Exception {
        // When: 첫 번째 채팅방 생성
        String roomId1 = chatRoomService.createOrGetChatRoom(posterId, accepterId);
        createdRoomId = roomId1;

        // When: 두 번째 채팅방 생성 (다른 accepterId)
        String accepterId2 = "accepter2-" + UUID.randomUUID();
        String roomId2 = chatRoomService.createOrGetChatRoom(posterId, accepterId2);

        try {
            // Then: 1. 두 방 ID가 다른지 확인
            assertThat(roomId1).isNotEqualTo(roomId2);
            System.out.println("✓ 채팅방 1 ID: " + roomId1);
            System.out.println("✓ 채팅방 2 ID: " + roomId2);

            // 2. 두 방이 모두 Firestore에 저장되었는지 확인
            var room1Doc = firestore.collection("chats").document(roomId1).get().get();
            var room2Doc = firestore.collection("chats").document(roomId2).get().get();
            assertThat(room1Doc.exists()).isTrue();
            assertThat(room2Doc.exists()).isTrue();
            System.out.println("✓ 두 채팅방 모두 Firestore에 저장됨");

            // 3. 각 방의 헬퍼 ID가 올바른지 확인
            assertThat(room1Doc.get("helperId")).isEqualTo(accepterId);
            assertThat(room2Doc.get("helperId")).isEqualTo(accepterId2);
            System.out.println("✓ 각 채팅방의 helperId 검증 완료");
        } finally {
            // 두 번째 방도 정리
            firestore.collection("chats").document(roomId2).delete().get();
        }
    }
}
