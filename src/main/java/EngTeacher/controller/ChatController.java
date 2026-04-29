package EngTeacher.controller;

import EngTeacher.dto.ChatMessageRequestDto;
import EngTeacher.dto.ChatMessageResponseDto;
import EngTeacher.model.Session;
import EngTeacher.model.User;
import EngTeacher.service.ChatService;
import EngTeacher.service.SessionService;
import EngTeacher.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat/")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;
    private final SessionService sessionService;

    @PostMapping("/message")
    public ChatMessageResponseDto processMessage(@RequestBody final ChatMessageRequestDto chatMessageRequestDto) {
        User user = userService.getUser(chatMessageRequestDto.getUserId());
        Session session = sessionService.getSession(user, chatMessageRequestDto.getSessionId());
        return chatService.processMessage(user, session, chatMessageRequestDto.getMessage());
    }
}
