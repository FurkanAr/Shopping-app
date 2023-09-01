package org.commerce.authenticationservice.service;

import org.commerce.authenticationservice.exception.messages.Messages;
import org.commerce.authenticationservice.exception.role.RoleCannotFoundException;
import org.commerce.authenticationservice.model.Role;
import org.commerce.authenticationservice.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    Logger logger = LoggerFactory.getLogger(getClass());

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }


    public Set<Role> getRoles(Set<String> roles) {
        logger.info("getRoles method started");
        Set<Role> roleSet = new HashSet<>();
        roles.forEach(role -> roleSet.add(roleRepository.findByName(role)
                .orElseThrow(() -> new RoleCannotFoundException(Messages.Role.NOT_EXISTS + role))));
        logger.info("roleSet: {}", roleSet);
        logger.info("getRoles method successfully worked");
        return roleSet;
    }
}
