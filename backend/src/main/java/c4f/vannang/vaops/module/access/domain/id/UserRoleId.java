package c4f.vannang.vaops.module.access.domain.id;

import lombok.*;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserRoleId implements Serializable {
    private UUID userId;
    private UUID role;
}
