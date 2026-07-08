package com.silkroad.market.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import com.silkroad.market.dto.chat.ChatResponse;
import com.silkroad.market.dto.chat.ChatSummaryResponse;
import com.silkroad.market.dto.chat.MessageResponse;
import com.silkroad.market.dto.chat.ReplyMessageRequest;
import com.silkroad.market.dto.chat.SendChatMessageRequest;
import com.silkroad.market.entity.Advertisement;
import com.silkroad.market.entity.AdvertisementStatus;
import com.silkroad.market.entity.Chat;
import com.silkroad.market.entity.Message;
import com.silkroad.market.entity.User;
import com.silkroad.market.exception.ApiException;
import com.silkroad.market.repository.AdvertisementRepository;
import com.silkroad.market.repository.ChatRepository;
import com.silkroad.market.repository.MessageRepository;
import com.silkroad.market.repository.UserRepository;

public class ChatService {
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final AdvertisementRepository advertisementRepository;
    private final UserRepository userRepository;

    public ChatService(
            ChatRepository chatRepository,
            MessageRepository messageRepository,
            AdvertisementRepository advertisementRepository,
            UserRepository userRepository) {

        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.advertisementRepository = advertisementRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void sendMessage(
            SendChatMessageRequest request,
            String username) {

        User buyer = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(
                        "User not found",
                        HttpStatus.NOT_FOUND));

        Advertisement advertisement = advertisementRepository
                .findById(request.getAdvertisementId())
                .orElseThrow(() -> new ApiException(
                        "Advertisement not found",
                        HttpStatus.NOT_FOUND));

        if (advertisement.getStatus() != AdvertisementStatus.APPROVED) {
            throw new ApiException(
                    "Advertisement is unavailable",
                    HttpStatus.BAD_REQUEST);
        }

        User seller = advertisement.getSeller();

        if (seller.getId().equals(buyer.getId())) {
            throw new ApiException(
                    "You cannot message yourself",
                    HttpStatus.BAD_REQUEST);
        }

        Chat chat = chatRepository
                .findByAdvertisementAndBuyerAndSeller(
                        advertisement,
                        buyer,
                        seller)
                .orElse(null);

        if (chat == null) {

            chat = new Chat();

            chat.setAdvertisement(advertisement);
            chat.setBuyer(buyer);
            chat.setSeller(seller);

            chatRepository.save(chat);
        }

        Message message = new Message();

        message.setChat(chat);
        message.setSender(buyer);
        message.setContent(request.getMessage());

        messageRepository.save(message);

        chat.setLastMessageAt(message.getSentAt());

        chatRepository.save(chat);
    }

    public List<ChatSummaryResponse> getChats(
            String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(
                        "User not found",
                        HttpStatus.NOT_FOUND));

        return chatRepository
                .findByBuyerOrSellerOrderByLastMessageAtDesc(
                        user,
                        user)
                .stream()
                .map(chat -> {

                    String otherUsername;

                    if (chat.getBuyer().getId().equals(user.getId())) {
                        otherUsername = chat.getSeller().getUsername();
                    } else {
                        otherUsername = chat.getBuyer().getUsername();
                    }

                    List<Message> messages = messageRepository.findByChatOrderBySentAtAsc(chat);

                    String lastMessage = "";

                    if (!messages.isEmpty()) {
                        lastMessage = messages
                                .get(messages.size() - 1)
                                .getContent();
                    }

                    return new ChatSummaryResponse(
                            chat.getId(),
                            chat.getAdvertisement().getId(),
                            chat.getAdvertisement().getTitle(),
                            otherUsername,
                            lastMessage,
                            chat.getLastMessageAt());
                })
                .toList();
    }

    @Transactional
    public void reply(
            Long chatId,
            ReplyMessageRequest request,
            String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(
                        "User not found",
                        HttpStatus.NOT_FOUND));

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ApiException(
                        "Chat not found",
                        HttpStatus.NOT_FOUND));

        if (!chat.getBuyer().getId().equals(user.getId())
                && !chat.getSeller().getId().equals(user.getId())) {

            throw new ApiException(
                    "Access denied",
                    HttpStatus.FORBIDDEN);
        }

        Message message = new Message();

        message.setChat(chat);
        message.setSender(user);
        message.setContent(request.getMessage());

        messageRepository.save(message);

        chat.setLastMessageAt(message.getSentAt());

        chatRepository.save(chat);
    }

    public ChatResponse getChat(
            Long chatId,
            String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(
                        "User not found",
                        HttpStatus.NOT_FOUND));

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ApiException(
                        "Chat not found",
                        HttpStatus.NOT_FOUND));

        if (!chat.getBuyer().getId().equals(user.getId())
                && !chat.getSeller().getId().equals(user.getId())) {

            throw new ApiException(
                    "Access denied",
                    HttpStatus.FORBIDDEN);
        }

        List<MessageResponse> messages = messageRepository
                .findByChatOrderBySentAtAsc(chat)
                .stream()
                .map(message -> new MessageResponse(
                        message.getSender().getUsername(),
                        message.getContent(),
                        message.getSentAt()))
                .toList();

        return new ChatResponse(
                chat.getId(),
                chat.getAdvertisement().getId(),
                chat.getAdvertisement().getTitle(),
                messages);
    }
}
