package com.semotone.semotone.domain.chat.dto;

import lombok.Getter;

@Getter
public class ChatRoomCreateRequest {
    // Getter, Setter
    private String accepterId;

    public void setAccepterId(String accepterId) { this.accepterId = accepterId; }
}
