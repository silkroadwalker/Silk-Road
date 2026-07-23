package com.silkroad.market.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
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

/**
 * Service class responsible for managing chat and messaging operations.
 * 
 * <p>
 * This service handles all chat-related functionality including creating
 * new chats between buyers and sellers, sending messages, retrieving chat
 * history, and managing replies. Each chat is associated with a specific
 * advertisement and connects the buyer and seller of that advertisement.
 * </p>
 * 
 * <p>
 * Chats are created automatically when a user sends the first message
 * to a seller. Users can only initiate chats for approved advertisements
 * and cannot message themselves.
 * </p>
 * 
 * @author Silkroad Market Team
 * @version 1.0
 * @see Chat
 * @see Message
 * @see ChatRepository
 * @see MessageRepository
 */
@Service
public class ChatService {
        private final ChatRepository chatRepository;
        private final MessageRepository messageRepository;
        private final AdvertisementRepository advertisementRepository;
        private final UserRepository userRepository;

        /**
         * Constructs a new ChatService with all required dependencies.
         * 
         * @param chatRepository          repository for chat persistence operations
         * @param messageRepository       repository for message persistence operations
         * @param advertisementRepository repository for advertisement lookups
         * @param userRepository          repository for user lookups
         */
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

        /**
         * Sends a new message to a seller regarding a specific advertisement.
         * 
         * <p>
         * This method creates a new chat if one doesn't already exist between
         * the buyer and seller for the given advertisement. Messages can only be
         * sent for approved advertisements, and the buyer must be different from
         * the seller (users cannot message themselves).
         * </p>
         * 
         * @param request  the message request containing advertisement ID and message
         *                 content
         * @param username the username of the sender (buyer)
         * @throws ApiException with NOT_FOUND status if the user or advertisement is
         *                      not found
         * @throws ApiException with BAD_REQUEST status if the advertisement is not
         *                      approved or the user is messaging themselves
         */
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

        /**
         * Retrieves all chats for a specific user.
         * 
         * <p>
         * This method returns a list of all chats that the user is involved in,
         * either as a buyer or seller. Chats are ordered by the last message time
         * in descending order, showing the most recent conversations first.
         * </p>
         * 
         * @param username the username of the user
         * @return a list of chat summaries containing basic conversation information
         * @throws ApiException with NOT_FOUND status if the user is not found
         */
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

        /**
         * Sends a reply message to an existing chat.
         * 
         * <p>
         * This method allows a user (either buyer or seller) to reply to an
         * existing chat. The user must be a participant in the chat to send
         * a message.
         * </p>
         * 
         * @param chatId   the ID of the chat to reply to
         * @param request  the reply request containing the message content
         * @param username the username of the user sending the reply
         * @throws ApiException with NOT_FOUND status if the user or chat is not found
         * @throws ApiException with FORBIDDEN status if the user is not a participant
         *                      in the chat
         */
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

        /**
         * Retrieves the full conversation history of a specific chat.
         * 
         * <p>
         * This method returns all messages in a chat in chronological order
         * (oldest to newest). The user must be a participant in the chat to
         * view its contents.
         * </p>
         * 
         * @param chatId   the ID of the chat to retrieve
         * @param username the username of the requesting user
         * @return a chat response containing the full message history
         * @throws ApiException with NOT_FOUND status if the user or chat is not found
         * @throws ApiException with FORBIDDEN status if the user is not a participant
         *                      in the chat
         */
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