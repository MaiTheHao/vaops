package c4f.vannang.vaops.module.identity.domain;

import c4f.vannang.vaops.shared.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_identities", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_identities_provider", columnNames = {"provider", "provider_user_id"})
})
public class UserIdentity extends BaseTimeEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "provider", nullable = false)
    private String provider;

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;
}
