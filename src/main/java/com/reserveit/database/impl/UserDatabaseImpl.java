package com.reserveit.database.impl;

import com.reserveit.database.interfaces.UserDatabase;
import com.reserveit.model.User;
import com.reserveit.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserDatabaseImpl implements UserDatabase {
    private final UserRepository userRepository;

    public UserDatabaseImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public void deleteById(UUID id) {
        userRepository.deleteById(id);
    }

    @Override
    public  boolean existsByEmail(String email){
        return userRepository.existsByEmail(email);

    }



}