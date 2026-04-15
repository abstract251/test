package com.test.oes.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class LoginUser implements UserDetails {

    private final Integer userId;
    private final String username;
    private final String displayName;
    private final AccountRole accountRole;
    private final String password;
    private final List<? extends GrantedAuthority> authorities;

    public LoginUser(Integer userId, String username, String displayName, AccountRole accountRole, String password) {
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.accountRole = accountRole;
        this.password = password;
        this.authorities = List.of(accountRole.asAuthority());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
