package EngTeacher.service;

import EngTeacher.dto.ChatMessageResponseDto;
import EngTeacher.model.Exercise;
import EngTeacher.model.Session;
import EngTeacher.model.User;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatModel chatModel;
    private final List<ToolCallback> tools;
    private final ToolCallingManager toolCallingManager;
    private final ChatMemory chatMemory;
    private final UserService userService;
    private final SessionService sessionService;
    private final Tracer tracer;

    public ChatMessageResponseDto processMessage(User user, Session session, String userMessage) {
        Span span = tracer.nextSpan().name("user-interaction");
        try (Tracer.SpanInScope ws = tracer.withSpan(span.start())) {
            span.tag("langfuse.user.id", user.getId());
            span.tag("langfuse.session.id", session.getId());
            span.tag("langfuse.observation.input", userMessage);

            ChatMessageResponseDto result = execute(user, session, userMessage);

            span.tag("langfuse.observation.output", result.getAgentResponse());
            return result;
        } catch (Exception e) {
            span.error(e);
            span.tag("error", true);
            span.tag("error.message", e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }

    private ChatMessageResponseDto execute(User user, Session session, String userMessage) {

        String conversationId = session.getId();

        ToolCallingChatOptions chatOptions = ToolCallingChatOptions.builder()
                .toolCallbacks(tools)
                .internalToolExecutionEnabled(false)
                .build();

        List<Message> agentLoopMemory = new ArrayList<>(chatMemory.get(conversationId));
        SystemMessage systemMessage = new SystemMessage(buildSystemPrompt(session));
        UserMessage userMsg = new UserMessage(userMessage);
        agentLoopMemory.addAll(List.of(systemMessage, userMsg));

        Prompt prompt = new Prompt(agentLoopMemory, chatOptions);
        ChatResponse chatResponse = chatModel.call(prompt);

        while (chatResponse.hasToolCalls()) {
            ToolExecutionResult toolResult = toolCallingManager.executeToolCalls(prompt, chatResponse);
            // ChatMemory cant store messages from TOOLS. Spring said, it is gonna be fixed.
            agentLoopMemory.addAll(toolResult.conversationHistory());

            prompt = new Prompt(agentLoopMemory, chatOptions);
            chatResponse = chatModel.call(prompt);
        }

        String finalResponse = chatResponse.getResult().getOutput().getText();
        chatMemory.add(conversationId, userMsg);
        chatMemory.add(conversationId, chatResponse.getResult().getOutput());

        user = userService.getUser(user.getId());
        session = sessionService.getSession(user, session.getId());
        List<Exercise> updatedExercises = session.getExercises();

        return ChatMessageResponseDto.builder()
                .agentResponse(finalResponse)
                .exercises(updatedExercises)
                .build();
    }

    private String buildSystemPrompt(Session session) {
        String exercisesFormatted = formatExercises(session.getExercises());

        return """
                You are a language learning assistant. Help the user practice exact English phrases through exercises.
                
                Exercises:
                %s
                
                RULES:
                - Mark correct ONLY if the user's sentence contains the EXACT target phrase. Minor tense/grammar adaptation is fine; synonyms are not.
                - If the user uses a synonym or paraphrase: do not mark correct, acknowledge the similarity, give a hint toward the exact phrase, repeat the question.
                - If incorrect: do not reveal the answer. Give a contextual hint, mark incorrect, present the question again.
                - If the user asks for the answer: reveal it with a brief explanation, then continue.
                - Grammar/meaning questions: answer directly, no tool call.
                - Never include tool call syntax or JSON in your response.
                
                EXAMPLES:
                "I bought a can of tuna." (target: 'a tin of') → "Close! Same idea, but we need a different expression. Hint: it's the British English version. Try: 'I opened ... beans.'"
                "She will lead the project." (target: 'be in charge of') → "Good, but we need a specific two-word phrase meaning to be responsible for something. Try again: 'Who will ... the project?'"
                "I went to buy a tin of tuna." → [mark correct] "Nice work! 'A tin of' — marked as done."
                """.formatted(exercisesFormatted);
    }

    private String formatExercises(List<Exercise> exercises) {
        return exercises.stream()
                .filter(exercise -> !exercise.isDone())
                .map(ex -> String.format(
                        "ID: %s | Phrase: \"%s\" | Question: \"%s\"",
                        ex.getId(),
                        ex.getPhrase().getContent(),
                        ex.getQuestion()
                ))
                .collect(Collectors.joining("\n"));
    }
}
