package com.reserveit.service;


import com.reserveit.dto.UserDto;

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

}
