package org.commerce.authenticationservice.security.service;

import org.commerce.authenticationservice.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private String userName;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

    private CustomUserDetails(String userName, String password, Collection<? extends GrantedAuthority> authorities) {
        this.userName = userName;
        this.password = password;
        this.authorities = authorities;
    }

    public static CustomUserDetails create(User user){
        List<GrantedAuthority> authorityList = new ArrayList<>();
        user.getRoles().forEach(role -> {
            authorityList.add(new SimpleGrantedAuthority(role.getName()));
        });
        return new CustomUserDetails(user.getUserName(), user.getPassword(), authorityList);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userName;
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
