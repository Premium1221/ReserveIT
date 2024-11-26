package com.reserveit.logic.impl;

import com.reserveit.database.interfaces.IUserDatabase;
import com.reserveit.dto.UserDto;
import com.reserveit.model.User;
import com.reserveit.logic.interfaces.UserService;
import com.reserveit.util.PasswordHasher;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImp implements UserService {
    private final IUserDatabase userDb;
    private final PasswordHasher passwordHasher;

    UserServiceImp(IUserDatabase userDb, PasswordHasher passwordEncoder) {
        this.userDb = userDb;
        this.passwordHasher = passwordEncoder;
    }
    @Override
    public UserDto createUser(UserDto userDto,String rawPassword) {
        User user = convertToEntity(userDto);
        user.setHashedPassword(passwordHasher.hashPassword(rawPassword));
        User savedUser = userDb.save(user);
        return convertToDto(savedUser);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userDb.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(UUID id) {
        return userDb.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));


    }

    @Override
    public void deleteUserById(UUID id) {
userDb.deleteById(id);
    }

    @Override
    public boolean updateUserDetails(UUID id, String newEmail, String newPhoneNumber) {
        Optional<User> user = userDb.findById(id);
        if (user.isPresent()) {
            User existingUser = user.get();
            existingUser.setEmail(newEmail);
            existingUser.setPhoneNumber(newPhoneNumber);
            return true;
        }
        return false;
    }

    @Override
    public int updatePassword(UUID id, String oldPassword, String newPassword) {
        Optional<User> user = userDb.findById(id);
        if (user.isPresent()) {
            User existingUser = user.get();
           if( passwordHasher.matches(oldPassword, existingUser.getHashedPassword())){
               existingUser.setHashedPassword(passwordHasher.hashPassword(newPassword));
               userDb.save(existingUser);
               return 0;// The profile is set up

           }
            return 1;// The passwords doesnt match

        }
        return 2; //The profile is not find
    }

    @Override
    public UserDto getUserByEmail(String email) {
        User user = userDb.findByEmail(email);
        if (user != null) {
            return convertToDto(user);
        }
        throw new IllegalArgumentException("User not found with email: " + email);
    }

    @Override
    public User getUserEntityByEmail(String email){
        User user = userDb.findByEmail(email);
        if (user != null) {
            return user;
        }
        throw new IllegalArgumentException("User not found with email: " + email);
    }
    private User convertToEntity(UserDto userDto) {
        User user = new User();
        user.setId(userDto.getId());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setUserRole(userDto.getRole());
        return user;
    }

    private UserDto convertToDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setEmail(user.getEmail());
        userDto.setPhoneNumber(user.getPhoneNumber());
        userDto.setRole(user.getUserRole());
        return userDto;
    }
}
