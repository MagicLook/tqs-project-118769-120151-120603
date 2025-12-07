package com.MagicLook.service;

import com.MagicLook.dto.UserRegistrationDTO;
import com.MagicLook.data.User;
import com.MagicLook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    public User register(UserRegistrationDTO dto) {
        // Verifica se username já existe
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username já está em uso");
        }
        
        // Verifica se email já existe
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email já está em uso");
        }
        
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setTelephone(dto.getTelephone());
        
        return userRepository.save(user);
    }
    
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username);
        
        if (user == null) {
            user = userRepository.findByEmail(username);
        }
        
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        
        return null; // Login falhou
    }
}