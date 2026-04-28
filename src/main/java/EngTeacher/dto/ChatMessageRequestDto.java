package EngTeacher.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatMessageRequestDto {
    private final String userId;
    private final String sessionId;
    private final String message;
}
