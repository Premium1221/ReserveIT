package com.reserveit.service.impl;

import com.reserveit.dto.UserDto;
import com.reserveit.model.User;
import com.reserveit.repository.UserRepository;
import com.reserveit.service.UserService;
import com.reserveit.util.PasswordHasher;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImp implements UserService {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    UserServiceImp(UserRepository userRepository, PasswordHasher passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordEncoder;
    }
    @Override
    public UserDto createUser(UserDto userDto,String rawPassword) {
        User user = convertToEntity(userDto);
        user.setHashedPassword(passwordHasher.hashPassword(rawPassword));
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(UUID id) {
        return userRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));


    }

    @Override
    public void deleteUserById(UUID id) {
userRepository.deleteById(id);
    }

    @Override
    public boolean updateUserDetails(UUID id, String newEmail, String newPhoneNumber) {
        Optional<User> user = userRepository.findById(id);
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
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User existingUser = user.get();
           if( passwordHasher.matches(oldPassword, existingUser.getHashedPassword())){
               existingUser.setHashedPassword(passwordHasher.hashPassword(newPassword));
               userRepository.save(existingUser);
               return 0;// The profile is set up

           }
            return 1;// The passwords doesnt match

        }
        return 2; //The profile is not find
    }

    @Override
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            return convertToDto(user);
        }
        throw new IllegalArgumentException("User not found with email: " + email);
    }
    private User convertToEntity(UserDto userDto) {
        User user = new User();
        user.setId(userDto.getId());
        user.setEmail(userDto.getEmail());
        user.setPhoneNumber(userDto.getPhoneNumber());
        return user;
    }

    // Convert Entity to DTO
    private UserDto convertToDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setPhoneNumber(user.getPhoneNumber());
        return userDto;
    }
}
