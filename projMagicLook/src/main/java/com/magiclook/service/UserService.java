package com.magiclook.service;

import com.magiclook.dto.UserRegistrationDTO;
import com.magiclook.data.User;
import com.magiclook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public User register(UserRegistrationDTO dto) {
        // Valida se as passwords coincidem
        if (!dto.passwordsMatch()) {
            logger.warn("Password mismatch for registration attempt");
            throw new IllegalArgumentException("As palavras-passe não coincidem");
        }
        
        // Verifica se username já existe
        if (userRepository.existsByUsername(dto.getUsername())) {
            logger.warn("Registration failed: username already exists");
            throw new IllegalArgumentException("Username já está em uso");
        }
        
        // Verifica se email já existe
        if (userRepository.existsByEmail(dto.getEmail())) {
            logger.warn("Registration failed: email already exists");
            throw new IllegalArgumentException("Email já está em uso");
        }
        
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setTelephone(dto.getTelephone());
        
        User savedUser = userRepository.save(user);
        logger.info("User registered successfully: {}", savedUser.getUsername());
        return savedUser;
    }
    
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username);
        
        if (user == null) {
            user = userRepository.findByEmail(username);
        }
        
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            logger.info("User login successful");
            return user;
        }
        
        logger.warn("Failed login attempt");
        return null; // Login falhou
    }
}