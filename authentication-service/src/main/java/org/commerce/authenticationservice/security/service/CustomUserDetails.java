package org.commerce.authenticationservice.security.service;

import org.commerce.authenticationservice.model.Privilege;
import org.commerce.authenticationservice.model.Role;
import org.commerce.authenticationservice.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

public class CustomUserDetails implements UserDetails {

    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    static Logger logger = LoggerFactory.getLogger(CustomUserDetails.class);

    private CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    public static CustomUserDetails create(User user) {
        logger.info("create method started");
        CustomUserDetails customUserDetails =
                new CustomUserDetails(user.getUsername(), user.getPassword(), getAuthorities(user.getRoles()));
        logger.info("User found with userDetails: {}", user.getUsername());
        logger.info("create method successfully worked");
        return customUserDetails;
    }

    private static Collection<? extends GrantedAuthority> getAuthorities(
            Set<Role> roles) {

        return getGrantedAuthorities(getPrivileges(roles));
    }

    private static Set<String> getPrivileges(Set<Role> roles) {

        Set<String> privileges = new HashSet<>();
        Set<Privilege> collection = new HashSet<>();
        for (Role role : roles) {
            privileges.add(role.getName());
            collection.addAll(role.getPrivileges());
        }
        for (Privilege item : collection) {
            privileges.add(item.getName());
        }
        return privileges;
    }

    private static List<GrantedAuthority> getGrantedAuthorities(Set<String> privileges) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String privilege : privileges) {
            authorities.add(new SimpleGrantedAuthority(privilege));
        }
        return authorities;
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
        return username;
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
