package tech.agrowerk.infrastructure.security.details;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import tech.agrowerk.infrastructure.model.core.enums.RoleType;

import java.util.Collection;
import java.util.List;


public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final RoleType role;
    private final boolean deleted;
    private final boolean active;
    private final boolean locked;
    private final boolean emailVerified;
    private final Integer tokenVersion;

    public CustomUserDetails(Long id, String email, String password, RoleType role,
                             boolean deleted, boolean active, boolean locked,
                             boolean emailVerified, Integer tokenVersion) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.deleted = deleted;
        this.active = active;
        this.locked = locked;
        this.emailVerified = emailVerified;
        this.tokenVersion = tokenVersion;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked && !deleted;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active && !deleted && emailVerified;
    }
}