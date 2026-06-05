package com.ddogalmap.domain.chat.enumtype;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Status {
    SENDING, SENT, FAILED;

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }
}
