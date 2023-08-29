package org.commerce.authenticationservice.security.service;

import org.commerce.authenticationservice.model.User;
import org.commerce.authenticationservice.repository.UserRepository;
import org.commerce.authenticationservice.exception.messages.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;
    Logger logger = LoggerFactory.getLogger(getClass());

    public CustomUserDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("loadUserByUsername method started");
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(Messages.User.NOT_EXISTS + username));
        logger.info("User found with username: {}", username);
        UserDetails userDetails = CustomUserDetails.create(user);
        logger.info("User found with userDetails: {}", userDetails);
        logger.info("loadUserByUsername method successfully worked");
        return userDetails;
    }
}
