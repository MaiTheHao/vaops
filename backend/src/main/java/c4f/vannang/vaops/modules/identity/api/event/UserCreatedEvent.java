package c4f.vannang.vaops.modules.identity.api.event;

import java.util.UUID;

public record UserCreatedEvent(
    UUID userId,
    String accountName
) {}
