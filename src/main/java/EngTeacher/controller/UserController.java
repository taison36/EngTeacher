package EngTeacher.controller;

import EngTeacher.dto.AddPhraseDto;
import EngTeacher.model.Phrase;
import EngTeacher.model.User;
import EngTeacher.model.UserSettings;
import EngTeacher.security.AuthUtils;
import EngTeacher.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public User getUser(@PathVariable final String userId) {
        AuthUtils.requireSelf(userId);
        return userService.getUser(userId);
    }

    @PostMapping("/{userId}/phrases")
    public User addPhrases(@PathVariable final String userId,
                           @RequestBody final List<AddPhraseDto> phrases) {
        AuthUtils.requireSelf(userId);
        User user = userService.getUser(userId);
        return userService.addPhrases(user, phrases);
    }

    @GetMapping("/{userId}/phrases")
    public List<Phrase> getPhrases(@PathVariable final String userId) {
        AuthUtils.requireSelf(userId);
        User user = userService.getUser(userId);
        return user.getPhrases();
    }

    @GetMapping("/{userId}/settings")
    public UserSettings getSettings(@PathVariable final String userId) {
        AuthUtils.requireSelf(userId);
        User user = userService.getUser(userId);
        return userService.getSettings(user);
    }

    @PutMapping("/{userId}/settings")
    public UserSettings updateSettings(@PathVariable final String userId,
                                       @RequestBody final UserSettings settings) {
        AuthUtils.requireSelf(userId);
        User user = userService.getUser(userId);
        return userService.updateSettings(user, settings);
    }
}
