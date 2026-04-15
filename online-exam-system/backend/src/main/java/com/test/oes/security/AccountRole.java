package com.test.oes.security;

import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Getter
public enum AccountRole {
    ADMIN("0", "ROLE_ADMIN"),
    TEACHER("1", "ROLE_TEACHER"),
    STUDENT("2", "ROLE_STUDENT");

    private final String dbValue;
    private final String authority;

    AccountRole(String dbValue, String authority) {
        this.dbValue = dbValue;
        this.authority = authority;
    }

    public SimpleGrantedAuthority asAuthority() {
        return new SimpleGrantedAuthority(authority);
    }
}
