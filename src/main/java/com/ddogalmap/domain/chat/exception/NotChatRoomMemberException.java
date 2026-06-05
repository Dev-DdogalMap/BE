package com.ddogalmap.domain.chat.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class NotChatRoomMemberException extends RuntimeException {

    public NotChatRoomMemberException(String message) {
        super(message);
    }
}
