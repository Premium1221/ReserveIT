package com.reserveit.database.interfaces;

import com.reserveit.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IUserDatabase {
    User findByEmail(String email);
    User save(User user);
    List<User> findAll();
    Optional<User> findById(UUID id);
    void deleteById(UUID id);
}