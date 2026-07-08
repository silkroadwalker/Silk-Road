package com.silkroad.market.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.silkroad.market.dto.chat.ChatResponse;
import com.silkroad.market.dto.chat.ChatSummaryResponse;
import com.silkroad.market.dto.chat.ReplyMessageRequest;
import com.silkroad.market.dto.chat.SendChatMessageRequest;
import com.silkroad.market.service.ChatService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chat")
@SecurityRequirement(name = "bearerAuth")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public void sendMessage(
            @Valid @RequestBody SendChatMessageRequest request,
            Authentication authentication) {

        chatService.sendMessage(
                request,
                authentication.getName());
    }

    @GetMapping
    public List<ChatSummaryResponse> getChats(
            Authentication authentication) {

        return chatService.getChats(
                authentication.getName());
    }

    @GetMapping("/{chatId}")
    public ChatResponse getChat(
            @PathVariable Long chatId,
            Authentication authentication) {

        return chatService.getChat(
                chatId,
                authentication.getName());
    }

    @PostMapping("/{chatId}")
    public void reply(
            @PathVariable Long chatId,
            @Valid @RequestBody ReplyMessageRequest request,
            Authentication authentication) {

        chatService.reply(
                chatId,
                request,
                authentication.getName());
    }
}