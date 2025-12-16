package com.magiclook.authentication;

import com.MagicLook.boundary.UserController;
import com.MagicLook.data.User;
import com.MagicLook.dto.UserRegistrationDTO;
import com.MagicLook.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerUnitTest {

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private UserRegistrationDTO registrationDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setTelephone("912345678");

        registrationDTO = new UserRegistrationDTO();
        registrationDTO.setUsername("testuser");
        registrationDTO.setEmail("test@example.com");
        registrationDTO.setPassword("password123");
        registrationDTO.setFirstName("Test");
        registrationDTO.setLastName("User");
        registrationDTO.setTelephone("912345678");
    }

    @Test
    void testShowRegisterForm() {
        String viewName = userController.showRegisterForm(model);
        assertEquals("register", viewName);
    }

    @Test
    void testRegister_WithValidData() {
        when(userService.register(any(UserRegistrationDTO.class))).thenReturn(testUser);
        when(bindingResult.hasErrors()).thenReturn(false);

        String viewName = userController.register(registrationDTO, bindingResult, model);
        assertEquals("redirect:/magiclook/login?success", viewName);
        verify(userService).register(any(UserRegistrationDTO.class));
    }

    @Test
    void testRegister_WithErrors() {
        when(bindingResult.hasErrors()).thenReturn(true);

        String viewName = userController.register(registrationDTO, bindingResult, model);
        assertEquals("register", viewName);
        verify(userService, never()).register(any(UserRegistrationDTO.class));
    }

    @Test
    void testRegister_WithExistingUsername() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.register(any(UserRegistrationDTO.class)))
                .thenThrow(new RuntimeException("Username já está em uso"));

        String viewName = userController.register(registrationDTO, bindingResult, model);
        assertEquals("register", viewName);
        verify(model).addAttribute(eq("error"), eq("Username já está em uso"));
    }

    @Test
    void testShowLoginForm() {
        String viewName = userController.showLoginForm(model);
        assertEquals("login", viewName);
    }
}