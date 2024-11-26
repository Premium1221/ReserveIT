package com.reserveit.logic.interfaces;


import com.reserveit.dto.UserDto;
import com.reserveit.model.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserDto createUser(UserDto userDto,String rawPassword);
    List<UserDto> getAllUsers();
    UserDto getUserById(UUID id);
    void deleteUserById(UUID id);
   boolean updateUserDetails(UUID id, String newEmail,String newpPhoneNumber);
   int updatePassword(UUID id, String oldPassword, String newPassword);
    UserDto getUserByEmail(String email);
    User getUserEntityByEmail(String username);

}
