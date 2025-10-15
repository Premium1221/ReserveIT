package com.reserveit.logic.impl;

import com.reserveit.database.interfaces.UserDatabase;
import com.reserveit.dto.RegisterRequest;
import com.reserveit.dto.UserDto;
import com.reserveit.enums.UserRole;
import com.reserveit.logic.interfaces.AdminService;
import com.reserveit.logic.interfaces.RefreshTokenService;
import com.reserveit.logic.interfaces.StaffService;
import com.reserveit.model.Staff;
import com.reserveit.model.User;
import com.reserveit.util.PasswordGenerator;
import com.reserveit.util.PasswordHasher;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {
    private final UserDatabase userDatabase;
    private final PasswordHasher passwordHasher;
    private final EmailServiceImpl emailService;
    private final PasswordGenerator passwordGenerator;
    private final RefreshTokenService refreshTokenService;
    private final StaffService staffService;

    public AdminServiceImpl(UserDatabase userDatabase,
                            PasswordHasher passwordHasher,
                            EmailServiceImpl emailService,
                            PasswordGenerator passwordGenerator,
                            RefreshTokenService refreshTokenService, StaffService staffService) {
        this.userDatabase = userDatabase;
        this.passwordHasher = passwordHasher;
        this.emailService = emailService;
        this.passwordGenerator = passwordGenerator;
        this.refreshTokenService = refreshTokenService;
        this.staffService = staffService;
    }

    @Override
    public void createUser(RegisterRequest request) {
        validateRequest(request);
        String generatedPassword = passwordGenerator.generateSecurePassword();

        try {
            UserRole role = UserRole.valueOf(request.getRole().toUpperCase());
            UserDto userDto = createUserDto(request, role);

            switch (role) {
                case MANAGER:
                    if (request.getCompanyId() == null) {
                        throw new IllegalArgumentException("Company ID is required for manager role");
                    }
                    Staff manager = staffService.createManager(userDto, request.getCompanyId(), generatedPassword);
                    emailService.sendUserCredentials(manager.getEmail(), manager.getFullName(), generatedPassword);
                    break;

                case STAFF:
                    Staff staff = staffService.createStaffMember(userDto, request.getCompanyId(), generatedPassword);
                    emailService.sendUserCredentials(staff.getEmail(), staff.getFullName(), generatedPassword);
                    break;

                default:
                    User user = buildUser(request, generatedPassword);
                    User savedUser = userDatabase.save(user);
                    emailService.sendUserCredentials(savedUser.getEmail(), savedUser.getFullName(), generatedPassword);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Failed to create user: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        User user = userDatabase.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        refreshTokenService.deleteAllUserTokens(user);
        userDatabase.deleteById(id);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userDatabase.findAll().stream()
                .map(this::convertToDto)
                .toList();
    }

    private void validateRequest(RegisterRequest request) {
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }

        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (request.getRole() == null) {
            throw new IllegalArgumentException("Role is required");
        }

        try {
            UserRole role = UserRole.valueOf(request.getRole().toUpperCase());
            if (role == UserRole.MANAGER && request.getCompanyId() == null) {
                throw new IllegalArgumentException("Company ID is required for manager role");
            }
        } catch (IllegalArgumentException e) {
            if (!"MANAGER".equalsIgnoreCase(request.getRole())) {
                throw new IllegalArgumentException("Invalid role: " + request.getRole());
            }
            throw e;
        }

        if (userDatabase.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Email already exists");
        }
    }


    private User buildUser(RegisterRequest request, String password) {
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setHashedPassword(passwordHasher.hashPassword(password));
        user.setUserRole(UserRole.valueOf(request.getRole().toUpperCase()));
        return user;
    }
    private UserDto createUserDto(RegisterRequest request, UserRole role) {
        UserDto userDto = new UserDto();
        userDto.setFirstName(request.getFirstName());
        userDto.setLastName(request.getLastName());
        userDto.setEmail(request.getEmail());
        userDto.setPhoneNumber(request.getPhoneNumber());
        userDto.setRole(role);
        return userDto;
    }

    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRole(user.getUserRole());
        return dto;
    }
}
