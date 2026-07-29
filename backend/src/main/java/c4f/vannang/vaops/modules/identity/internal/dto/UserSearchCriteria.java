package c4f.vannang.vaops.modules.identity.internal.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class UserSearchCriteria {

  private final int page;
  private final int size;
  private final String sortBy;
  private final Sort.Direction sortDirection;
  private final String keyword;
  private final Boolean isActive;

  public static final int DEFAULT_PAGE = 0;
  public static final int DEFAULT_SIZE = 20;
  public static final String DEFAULT_SORT_BY = "id";
  public static final Sort.Direction DEFAULT_SORT_DIRECTION = Sort.Direction.ASC;

  public UserSearchCriteria(int page, int size, String sortBy, Sort.Direction sortDirection,
      String keyword, Boolean isActive) {
    this.page = page >= 0 ? page : DEFAULT_PAGE;
    this.size = size > 0 ? size : DEFAULT_SIZE;
    this.sortBy = (sortBy != null && !sortBy.isBlank()) ? sortBy : DEFAULT_SORT_BY;
    this.sortDirection = sortDirection != null ? sortDirection : DEFAULT_SORT_DIRECTION;
    this.keyword = keyword;
    this.isActive = isActive;
  }

  public UserSearchCriteria() {
    this(DEFAULT_PAGE, DEFAULT_SIZE, DEFAULT_SORT_BY, DEFAULT_SORT_DIRECTION,
        null, null);
  }

  public Pageable toPageable() {
    return PageRequest.of(page, size, sortDirection, sortBy);
  }

  public int getPage() {
    return page;
  }

  public int getSize() {
    return size;
  }

  public String getSortBy() {
    return sortBy;
  }

  public Sort.Direction getSortDirection() {
    return sortDirection;
  }

  public String getKeyword() {
    return keyword;
  }

  public Boolean getIsActive() {
    return isActive;
  }
}
