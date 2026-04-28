package EngTeacher.controller;

import EngTeacher.model.Exercise;
import EngTeacher.model.Phrase;
import EngTeacher.model.Session;
import EngTeacher.model.User;
import EngTeacher.service.ExerciseService;
import EngTeacher.service.SessionService;
import EngTeacher.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
class UserController {

    private final UserService userService;
    private final SessionService sessionService;
    private final ExerciseService exerciseService;

    @PostMapping("/create")
    public User createUser(@RequestBody final String name) {
        return userService.createUser(name);
    }

    @PostMapping("/{userId}/phrases")
    public User addPhrases(@PathVariable final String userId,
                           @RequestBody final List<Phrase> phrases) {
        return userService.addPhrases(userId, phrases);
    }

    @GetMapping("{userId}/session/")
    public Session getSession(@PathVariable String userId,
                              @PathVariable String sessionId) {
        User user = userService.getUser(userId);
        return sessionService.getSession(user, sessionId);
    }

    @PostMapping("{userId}/session/")
    public Session createSession(@PathVariable String userId) {
        User user = userService.getUser(userId);
        return sessionService.createSession(user);
    }

    @PostMapping("{userId}/session/{sessionId}/exercise")
    public List<Exercise> createExercises(@PathVariable String userId,
                                          @PathVariable String sessionId) {
        User user = userService.getUser(userId);
        Session session= sessionService.getSession(user, sessionId);
        return exerciseService.createExercises(user, session);
    }
}
