package EngTeacher.dto.agent;

public record AgentDto() {

    public record CorrectExerciseAttempt(
            String userId,
            String exerciseId
    ) {
    }

    public record IncorrectExerciseAttempt(
            String userId,
            String exerciseId,
            String newQuestion
    ) {
    }
}
