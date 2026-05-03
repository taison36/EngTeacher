package EngTeacher.controller;

import EngTeacher.dto.AddPhraseDto;
import EngTeacher.model.User;
import EngTeacher.model.UserSettings;
import EngTeacher.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
class UserController {

    private final UserService userService;

    @PostMapping("/create")
    public User createUser(@RequestBody final String name) {
        return userService.createUser(name);
    }

    @GetMapping("/{userId}")
    public User getUser(@PathVariable final String userId) {
        return userService.getUser(userId);
    }

    @GetMapping("/by-name/{name}")
    public User getUserByName(@PathVariable final String name) {
        return userService.getUserByName(name);
    }

    @PostMapping("/{userId}/phrases")
    public User addPhrases(@PathVariable final String userId,
                           @RequestBody final List<AddPhraseDto> phrases) {
        User user = userService.getUser(userId);
        return userService.addPhrases(user, phrases);
    }

    @GetMapping("/{userId}/settings")
    public UserSettings getSettings(@PathVariable final String userId) {
        User user = userService.getUser(userId);
        return userService.getSettings(user);
    }

    @PutMapping("/{userId}/settings")
    public UserSettings updateSettings(@PathVariable final String userId,
                                       @RequestBody final UserSettings settings) {
        User user = userService.getUser(userId);
        return userService.updateSettings(user, settings);
    }
}
