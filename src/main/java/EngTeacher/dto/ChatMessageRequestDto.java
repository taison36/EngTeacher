package EngTeacher.dto;

import lombok.Data;

@Data
public class ChatMessageRequestDto {
    private String userId;
    private String sessionId;
    private String message;
}
