package EngTeacher.dto;

import EngTeacher.model.Exercise;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChatMessageResponseDto {
    private String agentResponse;
    private List<Exercise> exercises;
}
