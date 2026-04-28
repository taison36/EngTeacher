package EngTeacher.service;

import EngTeacher.constant.AppConstants;
import EngTeacher.dto.agent.AgentDto;
import EngTeacher.dto.agent.ExerciseGenerationDto;
import EngTeacher.exceptions.AgentResponseParsingException;
import EngTeacher.exceptions.ImproperUsageException;
import EngTeacher.model.Exercise;
import EngTeacher.model.Phrase;
import EngTeacher.model.Session;
import EngTeacher.model.User;
import EngTeacher.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    public List<Exercise> createExercises(User user, Session session) {
        List<Phrase> phrases = choosePhrases(user, session);

        if (phrases.isEmpty()) {
            throw new ImproperUsageException("No need to create exercises. Max is already reached");
        }

        String prompt = buildGenerateExercisesPrompt(phrases);

        String llmResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        try {
            List<ExerciseGenerationDto> exerciseDtos = objectMapper.readValue(
                    llmResponse,
                    new TypeReference<>() {
                    }
            );

            List<Exercise> createdExercises = exerciseDtos.stream()
                    .map(dto -> {
                        Phrase matchingPhrase = phrases.stream()
                                .filter(p -> p.getContent().equals(dto.getPhrase()))
                                .findFirst()
                                .orElseThrow();

                        return Exercise.builder()
                                .question(dto.getQuestion())
                                .phrase(matchingPhrase)
                                .build();
                    })
                    .toList();
            session.getExercises().addAll(createdExercises);
            userRepository.save(user);

            return createdExercises;
        } catch (JacksonException e) {
            throw new AgentResponseParsingException(String.format("Failed to parse exercise generation response: %s", e));
        }
    }

    private List<Phrase> choosePhrases(final User user, final Session session) {
        int neededExercises = AppConstants.MAX_NUMBER_EXERCISES - session.getExercises().size();

        Random random = new Random();

        return user.getPhrases().stream()
                // randomness + understanding rate
                .sorted((p1, p2) -> {
                    int weight1 = 100 - p1.getUnderstandingRate() + random.nextInt(20);
                    int weight2 = 100 - p2.getUnderstandingRate() + random.nextInt(20);
                    return Integer.compare(weight2, weight1);
                })
                .limit(neededExercises)
                .collect(Collectors.toList());
    }

    private String buildGenerateExercisesPrompt(final List<Phrase> phrases) {
        List<Map<String, String>> phrasesJson = phrases.stream()
                .map(phrase -> Map.of("phrase", phrase.getContent()))
                .collect(Collectors.toList());

        String phrasesJsonString = objectMapper.writeValueAsString(phrasesJson);

        return """
                You are a language learning assistant. Create one exercise question for each phrase.
                
                Requirements:
                - Generate realistic scenarios where the student must USE the target phrase in their answer
                - Questions should NOT contain the phrase itself
                - Make exercises conversational and practical
                
                Return a valid JSON array with this exact structure:
                [
                  {"phrase": "a tin of", "question": "You're at the grocery store buying food for your cat. What do you ask for?"},
                  {"phrase": "enjoy yourself", "question": "Your friend is going on vacation. What do you tell them?"}
                ]
                
                Phrases as JSON:
                %s
                
                Return ONLY the JSON array, no markdown, no code blocks, no explanations.
                """.formatted(phrasesJsonString);
    }

    public void markCorrect(List<AgentDto.CorrectExerciseAttempt> correctExerciseAttempts) {
        correctExerciseAttempts.forEach(correctExerciseAttempt -> correctExerciseAttempt.exerciseId());
    }

    public void markIncorrect(List<AgentDto.IncorrectExerciseAttempt> incorrectExerciseAttempts) {
    }
}
