package by.bsuir.chatservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(
		@NotBlank(message = "Message text is required")
		@Size(max = 4000, message = "Message too long")
		String messageText,

		String attachmentKey
) {}
