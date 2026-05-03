package EngTeacher.service;

import EngTeacher.dto.AddPhraseDto;
import EngTeacher.exceptions.NotFoundException;
import EngTeacher.model.Phrase;
import EngTeacher.model.User;
import EngTeacher.model.UserSettings;
import EngTeacher.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User getUser(final String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User %s was not found in DB", userId)));
    }

    public User getUserByName(final String name) {
        return userRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException(String.format("User with name '%s' was not found", name)));
    }

    public User createUser(final String name) {
        return userRepository.save(User.builder().name(name).build());
    }

    public User addPhrases(final User user, final List<AddPhraseDto> dtos) {
        List<Phrase> phrases = dtos.stream()
                .map(dto -> Phrase.builder()
                        .id(UUID.randomUUID().toString())
                        .content(dto.getContent())
                        .build())
                .toList();
        user.getPhrases().addAll(phrases);
        return userRepository.save(user);
    }

    public UserSettings getSettings(final User user) {
        return user.getSettings() != null ? user.getSettings() : UserSettings.builder().build();
    }

    public UserSettings updateSettings(final User user, final UserSettings settings) {
        user.setSettings(settings);
        return userRepository.save(user).getSettings();
    }

    public User save(final User user) {
        return userRepository.save(user);
    }
}
