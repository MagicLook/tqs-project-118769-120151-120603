package com.MagicLook.authentication;

import com.MagicLook.boundary.UserController;
import com.MagicLook.data.User;
import com.MagicLook.dto.UserRegistrationDTO;
import com.MagicLook.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private User testUser;
    private UserRegistrationDTO registrationDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
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
    void testShowRegisterForm_ShouldReturnRegisterPage() throws Exception {
        mockMvc.perform(get("/magiclook/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void testRegister_WithValidData_ShouldRedirectToLogin() throws Exception {
        when(userService.register(any(UserRegistrationDTO.class))).thenReturn(testUser);

        mockMvc.perform(post("/magiclook/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", registrationDTO.getUsername())
                .param("email", registrationDTO.getEmail())
                .param("password", registrationDTO.getPassword())
                .param("firstName", registrationDTO.getFirstName())
                .param("lastName", registrationDTO.getLastName())
                .param("telephone", registrationDTO.getTelephone()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/magiclook/login?success"));

        verify(userService).register(any(UserRegistrationDTO.class));
    }

    @Test
    void testRegister_WithExistingUsername_ShouldShowError() throws Exception {
        when(userService.register(any(UserRegistrationDTO.class)))
                .thenThrow(new RuntimeException("Username já está em uso"));

        mockMvc.perform(post("/magiclook/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", registrationDTO.getUsername())
                .param("email", registrationDTO.getEmail())
                .param("password", registrationDTO.getPassword())
                .param("firstName", registrationDTO.getFirstName())
                .param("lastName", registrationDTO.getLastName())
                .param("telephone", registrationDTO.getTelephone()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("error"));

        verify(userService).register(any(UserRegistrationDTO.class));
    }

    @Test
    void testShowLoginForm_ShouldReturnLoginPage() throws Exception {
        mockMvc.perform(get("/magiclook/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginRequest"));
    }

    @Test
    void testLogin_WithValidCredentials_ShouldRedirectToDashboard() throws Exception {
        when(userService.login(eq("testuser"), eq("password123"))).thenReturn(testUser);

        mockMvc.perform(post("/magiclook/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "testuser")
                .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/magiclook/dashboard"));

        verify(userService).login("testuser", "password123");
    }

    @Test
    void testLogin_WithInvalidCredentials_ShouldShowError() throws Exception {
        when(userService.login(eq("testuser"), eq("wrongpassword"))).thenReturn(null);

        mockMvc.perform(post("/magiclook/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "testuser")
                .param("password", "wrongpassword"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attributeExists("loginRequest"));

        verify(userService).login("testuser", "wrongpassword");
    }

    @Test
    void testShowDashboard_WithoutLogin_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/magiclook/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/magiclook/login"));
    }

    @Test
    void testLogout_ShouldInvalidateSessionAndRedirect() throws Exception {
        mockMvc.perform(get("/magiclook/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/magiclook/login?logout"));
    }

    @Test
    void testLogin_WithEmailInsteadOfUsername_ShouldWork() throws Exception {
        when(userService.login(eq("test@example.com"), eq("password123"))).thenReturn(testUser);

        mockMvc.perform(post("/magiclook/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "test@example.com")
                .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/magiclook/dashboard"));

        verify(userService).login("test@example.com", "password123");
    }
}