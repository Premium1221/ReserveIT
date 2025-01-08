package com.reserveit.logic.interfaces;

import com.reserveit.dto.RegisterRequest;
import com.reserveit.dto.UserDto;
import java.util.List;
import java.util.UUID;

public interface AdminService {
    void createUser(RegisterRequest request);
    void deleteUser(UUID id);
    List<UserDto> getAllUsers();
}
