package org.commerce.authenticationservice.startup;


import org.commerce.authenticationservice.model.Privilege;
import org.commerce.authenticationservice.model.Role;
import org.commerce.authenticationservice.repository.PrivilegeRepository;
import org.commerce.authenticationservice.repository.RoleRepository;
import org.commerce.authenticationservice.request.RegisterRequest;
import org.commerce.authenticationservice.service.AuthenticationService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Component
public class DataLoad {

    private final AuthenticationService authenticationService;
    private final RoleRepository roleRepository;
    private final PrivilegeRepository privilegeRepository;

    public DataLoad(AuthenticationService authenticationService, RoleRepository roleRepository, PrivilegeRepository privilegeRepository) {
        this.authenticationService = authenticationService;
        this.roleRepository = roleRepository;
        this.privilegeRepository = privilegeRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {

        Privilege privilegeUserRead = new Privilege("USER_READ");
        Privilege privilegeUserCreate = new Privilege("USER_CREATE");
        Privilege privilegeUserUpdate = new Privilege("USER_UPDATE");
        Privilege privilegeUserDelete = new Privilege("USER_DELETE");

        privilegeUserRead = privilegeRepository.save(privilegeUserRead);
        privilegeUserCreate = privilegeRepository.save(privilegeUserCreate);
        privilegeUserUpdate = privilegeRepository.save(privilegeUserUpdate);
        privilegeUserDelete = privilegeRepository.save(privilegeUserDelete);


        Privilege privilegeManagerRead = new Privilege("MANAGER_READ");
        Privilege privilegeManagerCreate = new Privilege("MANAGER_CREATE");
        Privilege privilegeManagerUpdate = new Privilege("MANAGER_UPDATE");
        Privilege privilegeManagerDelete = new Privilege("MANAGER_DELETE");

        privilegeManagerRead = privilegeRepository.save(privilegeManagerRead);
        privilegeManagerCreate = privilegeRepository.save(privilegeManagerCreate);
        privilegeManagerUpdate = privilegeRepository.save(privilegeManagerUpdate);
        privilegeManagerDelete = privilegeRepository.save(privilegeManagerDelete);

        Privilege privilegeAdminRead = new Privilege("ADMIN_READ");
        Privilege privilegeAdminCreate = new Privilege("ADMIN_CREATE");
        Privilege privilegeAdminUpdate = new Privilege("ADMIN_UPDATE");
        Privilege privilegeAdminDelete = new Privilege("ADMIN_DELETE");

        privilegeAdminRead = privilegeRepository.save(privilegeAdminRead);
        privilegeAdminCreate = privilegeRepository.save(privilegeAdminCreate);
        privilegeAdminUpdate = privilegeRepository.save(privilegeAdminUpdate);
        privilegeAdminDelete = privilegeRepository.save(privilegeAdminDelete);


        Role roleUser = new Role("ROLE_USER",
                Set.of(privilegeUserRead, privilegeUserCreate, privilegeUserUpdate, privilegeUserDelete));

        Role roleManager = new Role("ROLE_MANAGER",
                Set.of(privilegeManagerRead, privilegeManagerCreate, privilegeManagerUpdate, privilegeManagerDelete));

        Role roleAdmin = new Role("ROLE_ADMIN",
                Set.of(privilegeAdminRead, privilegeAdminCreate, privilegeAdminUpdate, privilegeAdminDelete,
                        privilegeManagerRead, privilegeManagerCreate, privilegeManagerUpdate, privilegeManagerDelete));

        roleRepository.save(roleUser);
        roleRepository.save(roleAdmin);
        roleRepository.save(roleManager);

        Set<String> userRoleSet = new HashSet<>();
        userRoleSet.add(roleUser.getName());

        Set<String> adminRoleSet = new HashSet<>();
        adminRoleSet.add(roleAdmin.getName());

        Set<String> managerRoleSet = new HashSet<>();
        managerRoleSet.add(roleManager.getName());

        RegisterRequest funda = new RegisterRequest("funda-kar", "Funda", "Kar",
                "fundakar@gmail.com", "Test-password123", adminRoleSet);

        RegisterRequest ali = new RegisterRequest("ali-aktar", "Ali", "Aktar",
                "aliaktar@gmail.com", "Test-password123", adminRoleSet);

        RegisterRequest zeynep = new RegisterRequest("zeynep-sever", "Zeynep", "Sever",
                "zeynepsever@gmail.com", "Test-password123", adminRoleSet);

        RegisterRequest can = new RegisterRequest("can-tok", "Can", "Tok",
                "cantok@gmail.com", "Test-password123", managerRoleSet);

        RegisterRequest akif = new RegisterRequest("akif-bıcak", "Akif", "Bıcak",
                "akif-bıcak@gmail.com", "Test-password123", managerRoleSet);

        RegisterRequest gizem = new RegisterRequest("gizem-ak", "Gizem", "Ak",
                "gizemak@gmail.com", "Test-password123", managerRoleSet);

        RegisterRequest selim = new RegisterRequest("selim-ak", "Selim", "Ak",
                "selimak@gmail.com", "Test-password123", userRoleSet);

        RegisterRequest seda = new RegisterRequest("seda-ak", "Seda", "Ak",
                "sedaak@gmail.com", "Test-password123", userRoleSet);

        RegisterRequest deniz = new RegisterRequest("deniz-ak", "Deniz", "Ak",
                "denizak@gmail.com", "Test-password123", userRoleSet);
        RegisterRequest ezgi = new RegisterRequest("ezgi-ak", "Ezgi", "Ak",
                "ezgiak@gmail.com", "Test-password123", userRoleSet);
        RegisterRequest cengiz = new RegisterRequest("cengiz-ak", "Cengiz", "Ak",
                "cengiz@gmail.com", "Test-password123", userRoleSet);

        List<RegisterRequest> requestList = List.of(funda, ali, zeynep, can, akif, gizem,
                selim, seda, deniz, ezgi, cengiz);

        requestList.forEach(authenticationService::register);


    }
}
