package com.MagicLook.services;

import com.MagicLook.data.User;
import com.MagicLook.dto.UserRegistrationDTO;
import com.MagicLook.repository.UserRepository;
import com.MagicLook.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserRegistrationDTO registrationDTO;
    private User existingUser;

    @BeforeEach
    void setUp() {
        registrationDTO = new UserRegistrationDTO();
        registrationDTO.setUsername("testuser");
        registrationDTO.setEmail("test@example.com");
        registrationDTO.setPassword("password123");
        registrationDTO.setFirstName("John");
        registrationDTO.setLastName("Doe");
        registrationDTO.setTelephone("912345678");

        existingUser = new User();
        existingUser.setUserId(UUID.randomUUID());
        existingUser.setUsername("existinguser");
        existingUser.setEmail("existing@example.com");
        existingUser.setPassword("password123");
        existingUser.setFirstName("Existing");
        existingUser.setLastName("User");
        existingUser.setTelephone("912345678");
    }

    @Test
    void testRegister_WithNewUser_ShouldReturnSavedUser() {
        when(userRepository.existsByUsername(registrationDTO.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registrationDTO.getEmail())).thenReturn(false);
        
        User savedUser = new User();
        savedUser.setUserId(UUID.randomUUID());
        savedUser.setUsername(registrationDTO.getUsername());
        savedUser.setEmail(registrationDTO.getEmail());
        savedUser.setPassword(registrationDTO.getPassword());
        savedUser.setFirstName(registrationDTO.getFirstName());
        savedUser.setLastName(registrationDTO.getLastName());
        savedUser.setTelephone(registrationDTO.getTelephone());
        
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.register(registrationDTO);

        assertNotNull(result);
        assertEquals(registrationDTO.getUsername(), result.getUsername());
        assertEquals(registrationDTO.getEmail(), result.getEmail());
        
        verify(userRepository).existsByUsername(registrationDTO.getUsername());
        verify(userRepository).existsByEmail(registrationDTO.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegister_WithExistingUsername_ShouldThrowException() {
        when(userRepository.existsByUsername(registrationDTO.getUsername())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.register(registrationDTO));
        
        assertEquals("Username já está em uso", exception.getMessage());
        verify(userRepository).existsByUsername(registrationDTO.getUsername());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_WithExistingEmail_ShouldThrowException() {
        when(userRepository.existsByUsername(registrationDTO.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registrationDTO.getEmail())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.register(registrationDTO));
        
        assertEquals("Email já está em uso", exception.getMessage());
        verify(userRepository).existsByUsername(registrationDTO.getUsername());
        verify(userRepository).existsByEmail(registrationDTO.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLogin_WithValidUsernameAndPassword_ShouldReturnUser() {
        String username = "testuser";
        String password = "password123";
        
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUsername(username);
        user.setPassword(password);
        
        when(userRepository.findByUsername(username)).thenReturn(user);

        User result = userService.login(username, password);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(password, result.getPassword());
        verify(userRepository).findByUsername(username);
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void testLogin_WithValidEmailAndPassword_ShouldReturnUser() {
        String email = "test@example.com";
        String password = "password123";
        
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail(email);
        user.setPassword(password);
        
        when(userRepository.findByUsername(email)).thenReturn(null);
        when(userRepository.findByEmail(email)).thenReturn(user);

        User result = userService.login(email, password);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(password, result.getPassword());
        verify(userRepository).findByUsername(email);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void testLogin_WithInvalidPassword_ShouldReturnNull() {
        String username = "testuser";
        String correctPassword = "password123";
        String wrongPassword = "wrongpassword";
        
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUsername(username);
        user.setPassword(correctPassword);
        
        when(userRepository.findByUsername(username)).thenReturn(user);

        User result = userService.login(username, wrongPassword);

        assertNull(result);
        verify(userRepository).findByUsername(username);
    }

    @Test
    void testLogin_WithNonExistentUser_ShouldReturnNull() {
        String username = "nonexistent";
        String password = "password123";
        
        when(userRepository.findByUsername(username)).thenReturn(null);
        when(userRepository.findByEmail(username)).thenReturn(null);

        User result = userService.login(username, password);

        assertNull(result);
        verify(userRepository).findByUsername(username);
        verify(userRepository).findByEmail(username);
    }
}