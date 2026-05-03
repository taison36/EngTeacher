package EngTeacher.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSettings {
    @Builder.Default
    private int maxNumberExercises = 10;
    @Builder.Default
    private int minUnderstandingRate = -100;
    @Builder.Default
    private int maxUnderstandingRate = 100;
}
