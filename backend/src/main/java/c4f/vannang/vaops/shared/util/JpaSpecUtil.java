package c4f.vannang.vaops.shared.util;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import java.util.Set;

public final class JpaSpecUtil {

  private JpaSpecUtil() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  @SuppressWarnings("unchecked")
  public static <Z, X> Join<Z, X> getOrCreateJoin(
      Root<Z> root, String attribute, JoinType joinType) {
    Set<Join<Z, ?>> rootJoins = root.getJoins();

    for (var join : rootJoins) {
      if (join.getAttribute().getName().equals(attribute)) return (Join<Z, X>) join;
    }

    return root.join(attribute, joinType);
  }
}
