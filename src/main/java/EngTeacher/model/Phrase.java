package EngTeacher.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Phrase {
    private final String id;
    private final String content;
    @Builder.Default
    private int understandingRate = 0;
    @Builder.Default
    private int completedExercises = 0;
}
