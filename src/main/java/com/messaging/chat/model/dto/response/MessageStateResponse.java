package com.messaging.chat.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response returned after updating delivered/read message state.")
public record MessageStateResponse(
        @Schema(description = "Conversation id.", example = "20")
        Long conversationId,
        @Schema(description = "User id that acknowledged the message.", example = "1")
        Long userId,
        @Schema(description = "Latest delivered/read message id stored for the user.", example = "100")
        Long messageId
) {
}
