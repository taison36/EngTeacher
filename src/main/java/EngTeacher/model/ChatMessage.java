package EngTeacher.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatMessage {
    public enum MessageType {
        AGENT,
        USER
    }
    private MessageType messageType;
    private String content;
}

