package EngTeacher.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Phrase {
    private String id;
    private String content;
    @Builder.Default
    private int understandingRate = 0;
    @Builder.Default
    private int completedExercises = 0;
}
