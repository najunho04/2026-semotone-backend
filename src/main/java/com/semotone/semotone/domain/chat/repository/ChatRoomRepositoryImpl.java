package com.semotone.semotone.domain.chat.repository;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class ChatRoomRepositoryImpl implements ChatRoomRepository{
    public static final String collection_name = "chats";
    private final Firestore firestore;

    @Override
    public String createOrGetChatRoom(String posterId, String accepterId) {
        try {
            // (선택 사항) 이미 둘 사이의 채팅방이 존재하는지 쿼리하는 로직을 추가할 수 있음

            // 1. 새로운 문서 참조(ID 자동 생성) 가져오기
            DocumentReference roomRef = firestore.collection(collection_name).document();

            // 2. 저장할 데이터 Map으로 구성
            Map<String, Object> roomData = new HashMap<>();
            roomData.put("users", Arrays.asList(posterId, accepterId)); // 검색용 배열
            roomData.put("requesterId", posterId);
            roomData.put("helperId", accepterId);
            roomData.put("lastMessage", ""); // 초기엔 빈 문자열
            // Firestore 서버의 현재 시간으로 기록
            //roomData.put("createdAt", com.google.cloud.firestore.FieldValue.serverTimestamp());
            roomData.put("lastMessageAt", com.google.cloud.firestore.FieldValue.serverTimestamp());
            //roomData.put("deleted", false);

            // 3. Firestore에 저장
            roomRef.set(roomData).get(); // .get()을 호출하여 비동기 작업이 끝날 때까지 대기

            // 4. 생성된 방의 ID 반환
            return roomRef.getId();

        } catch (Exception e) {
            throw new RuntimeException("채팅방 생성 중 오류 발생", e);
        }
    }
}
