package com.caoim.admin.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
public class AdminPrincipal implements UserDetails {
    private final Long id;
    private final String username;
    private final String password;
    private final String nickname;
    private final boolean superAdmin;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public AdminPrincipal(Long id, String username, String password, String nickname,
                          boolean superAdmin, boolean enabled,
                          Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.superAdmin = superAdmin;
        this.enabled = enabled;
        this.authorities = authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
