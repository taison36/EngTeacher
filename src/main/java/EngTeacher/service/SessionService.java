package EngTeacher.service;

import EngTeacher.exceptions.NotFoundException;
import EngTeacher.model.Session;
import EngTeacher.model.User;
import EngTeacher.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final UserRepository userRepository;
    private final ExerciseService exerciseService;

    public Session getSession(final User user, final String sessionId) {
        return  user.getSessions().stream()
                .filter(s -> s.getId().equals(sessionId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(String.format("Session %s was not found in DB", sessionId)));
    }

    public Session createSession(final User user) {
        Session createdSession = Session.builder().build();
        user.getSessions().add(createdSession);
        var exercises = exerciseService.createExercises(user, createdSession);
        createdSession.getExercises().addAll(exercises);
        userRepository.save(user);
        return createdSession;
    }
}
