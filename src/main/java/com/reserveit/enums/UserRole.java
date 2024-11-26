package com.reserveit.enums;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.*;
import java.util.stream.Collectors;
import com.reserveit.enums.Permission;


public enum UserRole {
    CUSTOMER(Set.of(
            Permission.RESERVATION_READ,
            Permission.RESERVATION_CREATE,
            Permission.RESERVATION_UPDATE_OWN,
            Permission.RESERVATION_CANCEL_OWN
    )),

    STAFF(Set.of(
            Permission.RESERVATION_READ,
            Permission.RESERVATION_UPDATE,
            Permission.TABLE_MANAGE,
            Permission.CUSTOMER_SERVICE
    )),

    MANAGER(Set.of(
            Permission.RESERVATION_READ,
            Permission.RESERVATION_CREATE,
            Permission.RESERVATION_UPDATE,
            Permission.RESERVATION_DELETE,
            Permission.STAFF_MANAGE,
            Permission.TABLE_MANAGE,
            Permission.COMPANY_VIEW,
            Permission.COMPANY_UPDATE,
            Permission.REPORT_VIEW
    )),

    ADMIN(Set.of(
            Permission.ADMIN_ACCESS,
            Permission.RESERVATION_READ,
            Permission.RESERVATION_CREATE,
            Permission.RESERVATION_UPDATE,
            Permission.RESERVATION_DELETE,
            Permission.STAFF_MANAGE,
            Permission.TABLE_MANAGE,
            Permission.COMPANY_MANAGE,
            Permission.USER_MANAGE,
            Permission.REPORT_VIEW,
            Permission.SYSTEM_MANAGE
    ));

    private final Set<Permission> permissions;

    UserRole(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public List<SimpleGrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // Add role-based authority
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));

        // Add permission-based authorities
        authorities.addAll(getPermissions()
                .stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toList()));

        return authorities;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }
}

