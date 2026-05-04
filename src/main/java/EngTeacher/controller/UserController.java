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

    @GetMapping
    public User getUser() {
        return userService.getUser(AuthUtils.currentUserId());
    }

    @PostMapping("/phrases")
    public User addPhrases(@RequestBody final List<AddPhraseDto> phrases) {
        User user = userService.getUser(AuthUtils.currentUserId());
        return userService.addPhrases(user, phrases);
    }

    @GetMapping("/phrases")
    public List<Phrase> getPhrases() {
        return userService.getUser(AuthUtils.currentUserId()).getPhrases();
    }

    @GetMapping("/settings")
    public UserSettings getSettings() {
        User user = userService.getUser(AuthUtils.currentUserId());
        return userService.getSettings(user);
    }

    @PutMapping("/settings")
    public UserSettings updateSettings(@RequestBody final UserSettings settings) {
        User user = userService.getUser(AuthUtils.currentUserId());
        return userService.updateSettings(user, settings);
    }
}
