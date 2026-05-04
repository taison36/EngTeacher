package EngTeacher.dto.agent;

public record ExerciseAttempt() {

    public record Correct(
            String exerciseId
    ) {
    }

    public record Incorrect(
            String exerciseId,
            String newQuestion
    ) {
    }
}
