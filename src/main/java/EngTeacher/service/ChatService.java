package EngTeacher.service;

import EngTeacher.dto.ChatMessageResponseDto;
import EngTeacher.model.Exercise;
import EngTeacher.model.Session;
import EngTeacher.model.User;
import EngTeacher.repo.UserRepository;
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

    public ChatMessageResponseDto processMessage(User user, Session session, String userMessage) {

        String conversationId = session.getId();

        ToolCallingChatOptions chatOptions = ToolCallingChatOptions.builder()
                .toolCallbacks(tools)
                .internalToolExecutionEnabled(false)
                .build();

        SystemMessage systemMessage = new SystemMessage(buildSystemPrompt(user.getId(), session));
        UserMessage userMsg = new UserMessage(userMessage);
        chatMemory.add(conversationId, List.of(systemMessage, userMsg));

        List<Message> localHistory = new ArrayList<>(chatMemory.get(conversationId));
        Prompt prompt = new Prompt(localHistory, chatOptions);
        ChatResponse chatResponse = chatModel.call(prompt);

        while (chatResponse.hasToolCalls()) {
            ToolExecutionResult toolResult = toolCallingManager.executeToolCalls(prompt, chatResponse);
            // ChatMemory cant store messages from TOOLS. Spring said, it is gonna be fixed.
            localHistory = new ArrayList<>(toolResult.conversationHistory());

            prompt = new Prompt(localHistory, chatOptions);
            chatResponse = chatModel.call(prompt);
        }

        user = userService.getUser(user.getId());
        session = sessionService.getSession(user, session.getId());
        List<Exercise> updatedExercises = session.getExercises();

        String finalResponse = chatResponse.getResult().getOutput().getText();
        chatMemory.add(conversationId, chatResponse.getResult().getOutput());

        return ChatMessageResponseDto.builder()
                .agentResponse(finalResponse)
                .exercises(updatedExercises)
                .build();
    }

    private String buildSystemPrompt(String userId, Session session) {
        String exercisesFormatted = formatExercises(session.getExercises());

        return """
                You are a friendly language learning assistant helping users practice English phrases through exercises.
                
                CONTEXT:
                User ID: %s
                Current exercises:
                %s
                
                YOUR WORKFLOW:
                
                1. EVALUATE exercise attempts:
                   - Correct usage: congratulate the user and call the tool to mark the exercise as done.
                   - Incorrect usage: do NOT reveal the target phrase or the correct answer. Call the tool to mark it incorrect and present the new generated
                question.
                   - If the user explicitly asks for the answer: reveal it, explain it briefly, then present the new generated question for this phrase.
                
                2. ANSWER questions directly:
                   - If the user asks about grammar or phrase meaning, answer without calling any tool.
                
                3. RESPOND naturally after each tool call, incorporating the result into the conversation.
                
                4. Skip exercises that are already marked as done.
                
                EXAMPLES:
                
                User: "I went to the store to buy a tin of tuna for dinner."
                → [mark correct]
                → "Perfect! You used 'a tin of' correctly. I've marked that exercise as done."
                
                User: "What does 'omit' mean?"
                → [no tool call]
                → "'Omit' means to leave out or exclude something."
                
                User: "I would ask my colleague to repeat herself."
                → [mark incorrect]
                → "Good effort, but that's not quite right. Let's try again: 'A colleague says your idea is too complex. You reply: ... that?'"
                
                User: "I don't understand. Can you tell me the correct answer?"
                → [mark incorrect]
                → "Of course! The answer is 'What do you mean by that?' — used when you want someone to clarify what they said. Here's your next question for this phrase: ..."
                """.formatted(userId, exercisesFormatted);
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
}
