package EngTeacher.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatMessageDto {
    private String content;
    private ChatMessageType type;

    public enum ChatMessageType {
        USER,
        ASSISTANT
    }
}
