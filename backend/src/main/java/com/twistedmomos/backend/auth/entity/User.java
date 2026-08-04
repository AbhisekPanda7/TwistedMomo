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
import java.util.Comparator;
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

    /**
     * Highest-privilege role. Backs the singular `role` field kept on the API and in
     * the JWT so clients written against one role keep working; authorization itself
     * uses the full set.
     */
    public RoleName primaryRole() {
        return roles.stream()
                .map(Role::getName)
                .max(Comparator.comparingInt(RoleName::precedence))
                .orElse(RoleName.CUSTOMER);
    }

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;
}
