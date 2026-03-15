package by.bsuir.chatservice.controller;

import by.bsuir.chatservice.dto.MessageResponse;
import by.bsuir.chatservice.dto.SendMessageRequest;
import by.bsuir.chatservice.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;






@Slf4j
@Controller
@ConditionalOnBean(SimpMessagingTemplate.class)
public class ChatWebSocketController {

	private final ChatService chatService;
	private final SimpMessagingTemplate messagingTemplate;

	@Autowired
	public ChatWebSocketController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
		this.chatService = chatService;
		this.messagingTemplate = messagingTemplate;
	}






	@MessageMapping("/chat.send/{orderId}")
	@SendTo("/topic/chat/{orderId}")
	public MessageResponse sendMessage(
			@DestinationVariable Long orderId,
			@Header("userId") Long userId,
			SendMessageRequest request) {
		log.info("WebSocket message received for order {} from user {}", orderId, userId);
		return chatService.sendMessage(orderId, userId, request);
	}






	@MessageMapping("/chat.read/{orderId}")
	public void markAsRead(
			@DestinationVariable Long orderId,
			@Header("userId") Long userId) {
		chatService.markMessagesAsRead(orderId, userId);
		messagingTemplate.convertAndSend("/topic/chat/" + orderId + "/read",
				java.util.Map.of("userId", userId, "orderId", orderId));
	}
}
