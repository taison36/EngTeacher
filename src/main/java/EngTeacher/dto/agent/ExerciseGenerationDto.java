package EngTeacher.dto.agent;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExerciseGenerationDto {
    private String phrase;
    private String question;
}
