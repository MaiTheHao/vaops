package c4f.vannang.vaops.modules.authorization.internal.dto;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record UserRoleSearchCriteria(
    UUID userId,
    UUID roleId,
    List<UUID> roleIds,
    Boolean isRevoked,
    int page,
    int size,
    String sortBy,
    String sortDirection
) {
  public Pageable toPageable() {
    int validPage = Math.max(0, page);
    int validSize = size <= 0 ? 20 : size;
    String validSortBy = (sortBy == null || sortBy.isBlank()) ? "createdAt" : sortBy;
    Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
    return PageRequest.of(validPage, validSize, Sort.by(direction, validSortBy));
  }
}
