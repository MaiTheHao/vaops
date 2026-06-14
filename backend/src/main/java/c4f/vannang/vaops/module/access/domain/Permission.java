package c4f.vannang.vaops.module.access.domain;

import c4f.vannang.vaops.shared.domain.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.Instant;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "permissions", uniqueConstraints = {
    @UniqueConstraint(name = "uk_permissions_business", columnNames = {"namespace", "resource", "code"})
})
public class Permission extends BaseAuditEntity {

    @Column(name = "namespace", nullable = false)
    @Builder.Default
    private String namespace = "system";

    @Column(name = "resource", nullable = false)
    private String resource;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by")
    private UUID deletedBy;

    @ManyToMany(mappedBy = "permissions")
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
}
