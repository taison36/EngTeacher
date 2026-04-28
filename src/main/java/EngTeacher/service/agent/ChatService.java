package EngTeacher.service.agent;

import EngTeacher.dto.ChatMessageResponseDto;
import EngTeacher.model.ChatMessage;
import EngTeacher.model.Exercise;
import EngTeacher.model.Session;
import EngTeacher.model.User;
import EngTeacher.repo.UserRepository;
import lombok.RequiredArgsConstructor;
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

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatModel chatModel;
    private final UserRepository userRepository;
    private final List<ToolCallback> tools;
    private final ToolCallingManager toolCallingManager;

    public ChatMessageResponseDto processMessage(User user, Session session, String userMessage) {

        ToolCallingChatOptions chatOptions = ToolCallingChatOptions.builder()
                .toolCallbacks(tools)
                .internalToolExecutionEnabled(false)
                .build();

        SystemMessage systemMessage = new SystemMessage(buildSystemPrompt(session));
        UserMessage userMsg = new UserMessage(userMessage);

        Prompt prompt = new Prompt(List.of(systemMessage, userMsg), chatOptions);

        ChatResponse chatResponse = chatModel.call(prompt);

        while (chatResponse.hasToolCalls()) {
            ToolExecutionResult toolResult = toolCallingManager.executeToolCalls(prompt, chatResponse);

            prompt = new Prompt(toolResult.conversationHistory(), chatOptions);

            chatResponse = chatModel.call(prompt);
        }

        userRepository.save(user);

        String finalResponse = chatResponse.getResult().getOutput().getText();

        return ChatMessageResponseDto.builder()
                .agentResponse(finalResponse)
                .exercises(session.getExercises())
                .build();
    }

    private String buildSystemPrompt(Session session) {
        String lastAgentMessage = getLastAgentMessage(session);
        String exercisesFormatted = formatExercises(session.getExercises());

        return """
                You are a language learning assistant helping users practice English phrases.
                
                CONTEXT:
                Last agent message:
                %s
                
                Current exercises:
                %s
                
                YOUR WORKFLOW:
                
                1. EVALUATE exercise attempts:
                   - If CORRECT: Call markExercisesCorrect([{userId, exerciseId}])
                   - If INCORRECT: Generate new question, call markExercisesIncorrect([{userId, exerciseId, newQuestion}])
                
                2. ANSWER questions directly:
                   - User asks about phrases/grammar → Just answer, no function call needed
                
                3. RESPOND naturally incorporating tool results and answers
                
                EXAMPLES:
                
                User: "I bought a tin of beans"
                → Call: markExercisesCorrect([{userId: "user123", exerciseId: "ex1"}])
                → Respond: "Excellent! You used 'a tin of' perfectly in context."
                
                User: "What does 'omit' mean?"
                → NO function call
                → Respond: "'Omit' means to leave out or exclude something."
                
                User: "I bought a tin of beans. What does 'omit' mean?"
                → Call: markExercisesCorrect([{userId: "user123", exerciseId: "ex1"}])
                → Respond: "Great job using 'a tin of'! Regarding 'omit'..."
                """.formatted(lastAgentMessage, exercisesFormatted);
    }

    private String formatExercises(List<Exercise> exercises) {
        return exercises.stream()
                .map(ex -> String.format(
                        "ID: %s | Phrase: \"%s\" | Question: \"%s\"",
                        ex.getId(),
                        ex.getPhrase().getContent(),
                        ex.getQuestion()
                ))
                .collect(Collectors.joining("\n"));
    }

    private String getLastAgentMessage(Session session) {
        List<ChatMessage> messages = session.getMessages();

        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatMessage msg = messages.get(i);
            if (msg.getMessageType() == ChatMessage.MessageType.AGENT) {
                return msg.getContent();
            }
        }

        return "No previous agent message";
    }
}
