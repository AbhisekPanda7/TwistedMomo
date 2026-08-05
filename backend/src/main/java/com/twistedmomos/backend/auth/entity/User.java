package com.twistedmomos.backend.auth.entity;

import com.twistedmomos.backend.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false, of = "id")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 190)
    private String email;

    /** BCrypt hash — never the raw password. */
    @Column(nullable = false, length = 100)
    private String password;

    @Column(length = 20)
    private String phone;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /** True when the user holds the given role. */
    public boolean hasRole(RoleName name) {
        return roles.stream().anyMatch(r -> r.getName() == name);
    }

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    /** Gates privilege, not purchase — an unverified user still browses and orders. */
    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private boolean emailVerified = false;
}
