package EngTeacher.controller;

import EngTeacher.dto.ChatMessageRequestDto;
import EngTeacher.dto.ChatMessageResponseDto;
import EngTeacher.model.Session;
import EngTeacher.model.User;
import EngTeacher.security.AuthUtils;
import EngTeacher.service.ChatService;
import EngTeacher.service.SessionService;
import EngTeacher.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;
    private final SessionService sessionService;

    @PostMapping("/message")
    public ChatMessageResponseDto processMessage(@RequestBody final ChatMessageRequestDto request) {
        User user = userService.getUser(AuthUtils.currentUserId());
        Session session = sessionService.getSession(user, request.getSessionId());
        return chatService.processMessage(user, session, request.getMessage());
    }
}
