package EngTeacher.tools;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.stereotype.Component;
import EngTeacher.dto.agent.AgentDto.IncorrectExerciseAttempt;
import EngTeacher.service.ExerciseService;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class IncorrectExerciseTool implements ToolCallback {

    private final ExerciseService exerciseService;
    private final ObjectMapper objectMapper;

    @Override
    public ToolDefinition getToolDefinition() {
        return ToolDefinition.builder()
                .name("markExercisesIncorrect")
                .description("Mark exercises as INCORRECT when user did not use phrases correctly. Provide new questions for failed exercises.")
                .inputSchema(buildInputSchema())
                .build();
    }

    @Override
    public String call(String toolInput) {
        try {
            // Parse JSON input
            Map<String, Object> input = objectMapper.readValue(toolInput, new TypeReference<>() {
            });

            // Extract attempts array
            List<IncorrectExerciseAttempt> attempts = objectMapper.convertValue(
                    input.get("attempts"),
                    new TypeReference<List<IncorrectExerciseAttempt>>() {
                    }
            );

            // Execute
            exerciseService.markIncorrect(attempts);

            return "Updated " + attempts.size() + " exercises as incorrect with new questions";

        } catch (JacksonException e) {
            throw new RuntimeException("Failed to parse tool input: " + toolInput, e);
        }
    }

    private String buildInputSchema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "attempts": {
                      "type": "array",
                      "description": "List of exercise attempts to mark as incorrect with new questions",
                      "items": {
                        "type": "object",
                        "properties": {
                          "userId": {
                            "type": "string",
                            "description": "ID of the user"
                          },
                          "exerciseId": {
                            "type": "string",
                            "description": "ID of the exercise that was answered incorrectly"
                          },
                          "newQuestion": {
                            "type": "string",
                            "description": "New question to replace the current one for this exercise"
                          }
                        },
                        "required": ["userId", "exerciseId", "newQuestion"]
                      }
                    }
                  },
                  "required": ["attempts"]
                }
                """;
    }
}
