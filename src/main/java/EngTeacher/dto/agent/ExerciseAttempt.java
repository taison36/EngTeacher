package EngTeacher.dto.agent;

public record ExerciseAttempt() {

    public record Correct(
            String userId,
            String exerciseId
    ) {
    }

    public record Incorrect(
            String userId,
            String exerciseId,
            String newQuestion
    ) {
    }
}
