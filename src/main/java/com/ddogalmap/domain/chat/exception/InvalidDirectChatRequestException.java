package com.ddogalmap.domain.chat.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidDirectChatRequestException extends RuntimeException {

    public InvalidDirectChatRequestException(String message) {
        super(message);
    }
}
