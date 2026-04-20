package com.messaging.chat.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request body for updating delivered/read message state.")
public record MessageStateRequest(
        @Schema(description = "Latest message id delivered or read by the user.", example = "100")
        @NotNull
        @Positive
        Long messageId
) {
}
