package io.github.uxlabspk.teamup.service;

import io.github.uxlabspk.teamup.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    
    User registerUser(User user);
    
    User updateUser(User user);
    
    Optional<User> getUserById(Long id);
    
    Optional<User> getUserByUsername(String username);
    
    Optional<User> getUserByEmail(String email);
    
    List<User> getAllUsers();
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    void deleteUser(Long id);
    
    void changePassword(Long userId, String newPassword);
    
    void updateProfilePicture(Long userId, String profilePicture);
    
    void updateStatus(Long userId, String status);
}