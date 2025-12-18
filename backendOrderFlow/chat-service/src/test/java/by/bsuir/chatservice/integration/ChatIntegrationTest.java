package by.bsuir.chatservice.integration;

import by.bsuir.chatservice.entity.ChatChannel;
import by.bsuir.chatservice.entity.Message;
import by.bsuir.chatservice.repository.ChatChannelRepository;
import by.bsuir.chatservice.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ChatIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChatChannelRepository channelRepository;

    @Autowired
    private MessageRepository messageRepository;

    private Long testOrderId;

    @BeforeEach
    void setUp() {
        testOrderId = ThreadLocalRandom.current().nextLong(100000, 999999);

        ChatChannel testChannel = ChatChannel.builder()
                .orderId(testOrderId)
                .supplierUserId(1L)
                .customerUserId(2L)
                .channelName("Test Channel")
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .build();
        testChannel = channelRepository.save(testChannel);

        Message testMessage = Message.builder()
                .channel(testChannel)
                .senderId(1L)
                .messageText("Test message content")
                .isRead(false)
                .sentAt(LocalDateTime.now())
                .build();
        messageRepository.save(testMessage);
    }

    @Test
    @DisplayName("Should get channel by order ID")
    void shouldGetChannelByOrderId() throws Exception {
        mockMvc.perform(get("/api/chats/order/" + testOrderId)
                        .header("X-User-Id", "1")
                        .header("X-User-Company-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should get user channels")
    void shouldGetUserChannels() throws Exception {
        mockMvc.perform(get("/api/chats")
                        .header("X-User-Id", "1")
                        .header("X-User-Company-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
