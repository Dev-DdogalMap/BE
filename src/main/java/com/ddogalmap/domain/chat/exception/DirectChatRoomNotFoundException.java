package com.ddogalmap.domain.chat.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class DirectChatRoomNotFoundException extends RuntimeException {

    public DirectChatRoomNotFoundException(String message) {
        super(message);
    }
}
