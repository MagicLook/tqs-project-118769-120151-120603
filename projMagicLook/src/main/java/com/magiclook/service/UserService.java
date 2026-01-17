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
    
    @Autowired
    private UserRepository userRepository;
    
    public User register(UserRegistrationDTO dto) {
        // Valida se as passwords coincidem
        if (!dto.passwordsMatch()) {
            logger.warn("Password mismatch for registration attempt with username: {}", dto.getUsername());
            throw new RuntimeException("As palavras-passe não coincidem");
        }
        
        // Verifica se username já existe
        if (userRepository.existsByUsername(dto.getUsername())) {
            logger.warn("Username already exists: {}", dto.getUsername());
            throw new RuntimeException("Username já está em uso");
        }
        
        // Verifica se email já existe
        if (userRepository.existsByEmail(dto.getEmail())) {
            logger.warn("Email already exists: {}", dto.getEmail());
            throw new RuntimeException("Email já está em uso");
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
            logger.info("User login successful: {}", username);
            return user;
        }
        
        logger.warn("Failed login attempt for username/email: {}", username);
        return null; // Login falhou
    }
}