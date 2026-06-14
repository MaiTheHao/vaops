package c4f.vannang.vaops.module.identity.domain;

import c4f.vannang.vaops.shared.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.Instant;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_credentials")
public class UserCredential extends BaseTimeEntity {

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "password_changed_at")
    private Instant passwordChangedAt;

    @Column(name = "failed_login_count", nullable = false)
    @Builder.Default
    private int failedLoginCount = 0;

    @Column(name = "locked_until")
    private Instant lockedUntil;
}
