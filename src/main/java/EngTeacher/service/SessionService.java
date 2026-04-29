package EngTeacher.service;

import EngTeacher.constant.AppConstants;
import EngTeacher.exceptions.NotFoundException;
import EngTeacher.model.Session;
import EngTeacher.model.User;
import EngTeacher.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final UserRepository userRepository;
    private final ExerciseService exerciseService;

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
        final var exercises = exerciseService.createExercises(user, neededExerciseQuantity);
        createdSession.getExercises().addAll(exercises);
        user.getSessions().add(createdSession);
        return createdSession;
    }

    public int neededExerciseQuantity(final Session session) {
        return AppConstants.MAX_NUMBER_EXERCISES - session.getExercises().size();
    }
}
