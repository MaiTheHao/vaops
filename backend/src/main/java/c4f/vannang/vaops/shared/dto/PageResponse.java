package c4f.vannang.vaops.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

@Schema(description = "Paginated data wrapper response")
public record PageResponse<T>(
    @Schema(description = "List of items on current page")
    List<T> content,

    @Schema(description = "Current page index (0-based)", example = "0")
    int page,

    @Schema(description = "Page size limit", example = "20")
    int size,

    @Schema(description = "Total number of elements across all pages", example = "100")
    long totalElements,

    @Schema(description = "Total number of pages", example = "5")
    int totalPages,

    @Schema(description = "Whether a next page exists", example = "true")
    boolean hasNext,

    @Schema(description = "Whether a previous page exists", example = "false")
    boolean hasPrevious) {


  public static <T> PageResponse<T> from(Page<T> page) {
    return new PageResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.hasNext(),
        page.hasPrevious());
  }

  public static <T, R> PageResponse<R> from(Page<T> page, Function<T, R> mapper) {
    return new PageResponse<>(
        page.getContent().stream().map(mapper).toList(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.hasNext(),
        page.hasPrevious());
  }

  public static <T> PageResponse<T> empty() {
    return new PageResponse<>(List.of(), 0, 0, 0L, 0, false, false);
  }
}
