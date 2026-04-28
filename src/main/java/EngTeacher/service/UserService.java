package EngTeacher.service;

import EngTeacher.exceptions.NotFoundException;
import EngTeacher.model.Phrase;
import EngTeacher.model.Session;
import EngTeacher.model.User;
import EngTeacher.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User getUser(final String userId) {
        var userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new NotFoundException(String.format("User %s was not found in DB", userId));
        }
        return userOpt.get();
    }

    public User createUser(final String name) {
        User user = User.builder()
                .name(name)
                .build();
        return userRepository.save(user);
    }

    public User addPhrases(String userId, List<Phrase> phrases) {
        User user = getUser(userId);
        user.getPhrases().addAll(phrases);
        return userRepository.save(user);
    }
}
