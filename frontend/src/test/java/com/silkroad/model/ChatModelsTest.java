package com.silkroad.model;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for the chat-related models: {@link ChatMessage},
 * {@link ChatSummary} and {@link ChatDetail}.
 *
 * Note: at the moment ChatMessage has no "seen/read" flag yet, so
 * these tests only cover the fields that currently exist. Once the
 * backend exposes a "read" status (task #7 in the project TODO list),
 * a corresponding field + test (e.g. isRead()/setRead()) should be
 * added here too.
 */
public class ChatModelsTest {

    @Test
    public void chatMessageGettersReturnValuesPassedToSetters() {
        ChatMessage message = new ChatMessage();
        message.setSenderUsername("ali92");
        message.setContent("Hello, is this still available?");
        message.setSentAt("2026-07-20T09:30:00");

        assertEquals("ali92", message.getSenderUsername());
        assertEquals("Hello, is this still available?", message.getContent());
        assertEquals("2026-07-20T09:30:00", message.getSentAt());
    }

    @Test
    public void chatSummaryGettersReturnValuesPassedToSetters() {
        ChatSummary summary = new ChatSummary();
        summary.setChatId(10L);
        summary.setAdvertisementId(5L);
        summary.setAdvertisementTitle("iPhone 13");
        summary.setOtherUsername("sara_88");
        summary.setLastMessage("Sure, still available");
        summary.setLastMessageTime("2026-07-21T14:00:00");

        assertEquals(Long.valueOf(10L), summary.getChatId());
        assertEquals(Long.valueOf(5L), summary.getAdvertisementId());
        assertEquals("iPhone 13", summary.getAdvertisementTitle());
        assertEquals("sara_88", summary.getOtherUsername());
        assertEquals("Sure, still available", summary.getLastMessage());
        assertEquals("2026-07-21T14:00:00", summary.getLastMessageTime());
    }

    @Test
    public void chatDetailHoldsListOfMessagesInOrder() {
        ChatMessage first = new ChatMessage();
        first.setSenderUsername("ali92");
        first.setContent("Hi");

        ChatMessage second = new ChatMessage();
        second.setSenderUsername("sara_88");
        second.setContent("Hello!");

        List<ChatMessage> messages = Arrays.asList(first, second);

        ChatDetail detail = new ChatDetail();
        detail.setChatId(10L);
        detail.setAdvertisementId(5L);
        detail.setAdvertisementTitle("iPhone 13");
        detail.setMessages(messages);

        assertEquals(Long.valueOf(10L), detail.getChatId());
        assertEquals(2, detail.getMessages().size());
        assertEquals("ali92", detail.getMessages().get(0).getSenderUsername());
        assertEquals("sara_88", detail.getMessages().get(1).getSenderUsername());
    }

    @Test
    public void chatDetailWithNoMessagesYetIsNull() {
        ChatDetail detail = new ChatDetail();
        assertNull(detail.getMessages());
    }
}
