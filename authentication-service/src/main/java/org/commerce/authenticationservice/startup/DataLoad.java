package org.commerce.authenticationservice.startup;


import org.commerce.authenticationservice.model.Role;
import org.commerce.authenticationservice.repository.RoleRepository;
import org.commerce.authenticationservice.request.UserRequest;
import org.commerce.authenticationservice.service.AuthenticationService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataLoad {

    private final AuthenticationService authenticationService;
    private final RoleRepository roleRepository;

    public DataLoad(AuthenticationService authenticationService, RoleRepository roleRepository) {
        this.authenticationService = authenticationService;
        this.roleRepository = roleRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        Role roleUser = new Role("ROLE_USER");
        Role roleAdmin = new Role("ROLE_ADMIN");

        roleRepository.save(roleUser);
        roleRepository.save(roleAdmin);

        Set<String> roles = new HashSet<>();
        roles.add(roleUser.getName());
        roles.add(roleAdmin.getName());

        authenticationService.register(new UserRequest("tester", "test-user",
                "test-user-surname", "tester@gmail.com", "Test-password123", roles));

    }
}
