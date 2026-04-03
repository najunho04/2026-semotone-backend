package com.semotone.semotone.domain.chat.service;

import com.semotone.semotone.domain.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService{

    private final ChatRoomRepository chatRoomRepository;

    @Override
    public String createOrGetChatRoom(String posterId, String accepterId) {
        return chatRoomRepository.createOrGetChatRoom(posterId, accepterId);
    }
}
