package EngTeacher.config;

import EngTeacher.dto.agent.AgentDto;
import EngTeacher.service.ExerciseService;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.internal.Function;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class AgentToolsConfig {

    private final ExerciseService exerciseService;

    @Bean
    @Description("Mark exercises as CORRECT when user successfully used the target phrases. Updates exercise status and phrase understanding rate.")
    public Function<List<AgentDto.CorrectExerciseAttempt>, String> markExercisesCorrect() {
        return attempts -> {
            exerciseService.markCorrect(attempts);
            return "Updated " + attempts.size() + " exercises";
        };
    }

    @Bean
    @Description("Mark exercises as INCORRECT when user did not use phrases correctly. Provide a new question for each failed exercise.")
    public Function<List<AgentDto.IncorrectExerciseAttempt>, String> markExercisesIncorrect() {
        return attempts -> {
            exerciseService.markIncorrect(attempts);
            return "Updated " + attempts.size() + " exercises";
        };
    }
}
