package com.semotone.semotone.domain.chat.repository;

public interface ChatRoomRepository {

    //유저 2명의 토큰을 검증 후 채팅방 ID를 리턴합니다.
    String createOrGetChatRoom(String posterId, String accepterId);
}
