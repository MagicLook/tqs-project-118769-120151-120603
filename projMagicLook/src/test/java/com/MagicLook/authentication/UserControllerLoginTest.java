package com.MagicLook.authentication;

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

import jakarta.servlet.http.HttpSession;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerLoginTest {

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private HttpSession session;

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
    void testLogin_WithValidCredentials_ShouldSetSessionAndRedirect() {
        String username = "testuser";
        String password = "password123";
        
        when(userService.login(username, password)).thenReturn(testUser);

        String viewName = userController.login(username, password, session, model);

        assertEquals("redirect:/magiclook/dashboard", viewName);
        verify(session).setAttribute(eq("loggedInUser"), eq(testUser));
        verify(session).setAttribute(eq("userId"), eq(testUser.getUserId()));
        verify(session).setAttribute(eq("userName"), eq(testUser.getFirstName()));
        verify(userService).login(username, password);
        verifyNoInteractions(model);
    }

    @Test
    void testLogin_WithEmailInsteadOfUsername_ShouldWork() {
        String email = "test@example.com";
        String password = "password123";
        
        when(userService.login(email, password)).thenReturn(testUser);

        String viewName = userController.login(email, password, session, model);

        assertEquals("redirect:/magiclook/dashboard", viewName);
        verify(session).setAttribute(eq("loggedInUser"), eq(testUser));
        verify(userService).login(email, password);
    }

    @Test
    void testLogin_WithInvalidCredentials_ShouldShowError() {
        String username = "testuser";
        String password = "wrongpassword";
        
        when(userService.login(username, password)).thenReturn(null);

        String viewName = userController.login(username, password, session, model);

        assertEquals("login", viewName);
        verify(model).addAttribute(eq("error"), eq("Usuário ou senha inválidos!"));
        verify(model).addAttribute(eq("loginRequest"), any());
        verify(session, never()).setAttribute(anyString(), any());
        verify(userService).login(username, password);
    }

    @Test
    void testLogin_WithEmptyCredentials_ShouldShowError() {
        String username = "";
        String password = "";
        
        when(userService.login(username, password)).thenReturn(null);

        String viewName = userController.login(username, password, session, model);

        assertEquals("login", viewName);
        verify(model).addAttribute(eq("error"), eq("Usuário ou senha inválidos!"));
        verify(userService).login(username, password);
    }
    
    @Test
    void testLogout_ShouldInvalidateSessionAndRedirect() {
        String viewName = userController.logout(session);

        assertEquals("redirect:/magiclook/login?logout", viewName);
        verify(session).invalidate();
    }

    @Test
    void testLogout_WithNullSession_ShouldStillRedirect() {
        String viewName = userController.logout(null);

        assertEquals("redirect:/magiclook/login?logout", viewName);
    }
    
    @Test
    void testRegister_WithExistingEmail_ShouldShowError() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.register(any(UserRegistrationDTO.class)))
                .thenThrow(new RuntimeException("Email já está em uso"));

        String viewName = userController.register(registrationDTO, bindingResult, model);

        assertEquals("register", viewName);
        verify(model).addAttribute(eq("error"), eq("Email já está em uso"));
        verify(userService).register(any(UserRegistrationDTO.class));
    }

    @Test
    void testRegister_WithGeneralException_ShouldShowError() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.register(any(UserRegistrationDTO.class)))
                .thenThrow(new RuntimeException("Erro inesperado"));

        String viewName = userController.register(registrationDTO, bindingResult, model);

        assertEquals("register", viewName);
        verify(model).addAttribute(eq("error"), eq("Erro inesperado"));
    }
}