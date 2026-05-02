package EngTeacher.service;

import EngTeacher.constant.AppConstants;
import EngTeacher.dto.ChatMessageDto;
import EngTeacher.exceptions.NotFoundException;
import EngTeacher.model.Session;
import EngTeacher.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final ExerciseGenerationService exerciseGenerationService;
    private final ChatMemory chatMemory;

    public Session getSession(final User user, final String sessionId) {
        return user.getSessions().stream()
                .filter(s -> s.getId().equals(sessionId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(String.format("Session %s was not found in DB", sessionId)));
    }

    public Session createSession(final User user) {
        final Session createdSession = Session.builder()
                .id(UUID.randomUUID().toString())
                .build();
        final int neededExerciseQuantity = neededExerciseQuantity(createdSession);
        final var exercises = exerciseGenerationService.generate(user, neededExerciseQuantity);
        createdSession.getExercises().addAll(exercises);
        user.getSessions().add(createdSession);
        return createdSession;
    }

    public int neededExerciseQuantity(final Session session) {
        return AppConstants.MAX_NUMBER_EXERCISES - session.getExercises().size();
    }

    public List<ChatMessageDto> getMessages(final String sessionId) {
        List<Message> chatMemoryMessages = chatMemory.get(sessionId);
        return chatMemoryMessages.stream()
                .filter(message -> message.getMessageType() == MessageType.USER ||
                        message.getMessageType() == MessageType.ASSISTANT)
                .map(message -> ChatMessageDto.builder()
                        .content(message.getText())
                        .type(message.getMessageType() == MessageType.USER ?
                                ChatMessageDto.ChatMessageType.USER :
                                ChatMessageDto.ChatMessageType.ASSISTANT)
                        .build())
                .toList();
    }
}
