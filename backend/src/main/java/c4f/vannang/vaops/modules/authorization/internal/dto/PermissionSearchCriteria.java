package c4f.vannang.vaops.modules.authorization.internal.dto;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record PermissionSearchCriteria(
    String keyword,
    String resource,
    String action,
    Boolean isActive,
    Collection<UUID> roleIds,
    Instant createdFrom,
    Instant createdTo,
    int page,
    int size,
    String sortBy,
    String sortDirection
) {
  public Pageable toPageable() {
    int validPage = Math.max(0, page);
    int validSize = size <= 0 ? 20 : size;
    String validSortBy = (sortBy == null || sortBy.isBlank()) ? "resource" : sortBy;
    Sort.Direction direction = "DESC".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
    return PageRequest.of(validPage, validSize, Sort.by(direction, validSortBy));
  }
}
