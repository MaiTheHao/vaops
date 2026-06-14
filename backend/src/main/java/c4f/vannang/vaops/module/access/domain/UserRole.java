package c4f.vannang.vaops.module.access.domain;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import c4f.vannang.vaops.module.access.domain.id.UserRoleId;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_roles")
@IdClass(UserRoleId.class)
public class UserRole implements Serializable {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "assigned_at", nullable = false)
    @Builder.Default
    private Instant assignedAt = Instant.now();

    @Column(name = "assigned_by")
    private UUID assignedBy;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "revoked_by")
    private UUID revokedBy;
}
