package com.magiclook.services;

import com.MagicLook.data.Staff;
import com.MagicLook.data.Shop;
import com.MagicLook.repository.StaffRepository;
import com.MagicLook.repository.ShopRepository;
import com.MagicLook.service.StaffService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffServiceTest {

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private ShopRepository shopRepository;

    @InjectMocks
    private StaffService staffService;

    private Staff testStaff;
    private Shop testShop;

    @BeforeEach
    void setUp() {
        testShop = new Shop();
        testShop.setShopId(1);
        testShop.setName("Loja Teste");
        testShop.setLocation("Localização Teste");

        testStaff = new Staff();
        testStaff.setStaffId(UUID.randomUUID());
        testStaff.setName("Staff Teste");
        testStaff.setEmail("staff@test.com");
        testStaff.setUsername("staffuser");
        testStaff.setPassword("password123");
        testStaff.setShop(testShop);
    }

    @Test
    void testLogin_WithValidEmail_ShouldReturnStaff() {
        String email = "staff@test.com";
        String password = "password123";
        
        when(staffRepository.findByEmail(email)).thenReturn(Optional.of(testStaff));

        Staff result = staffService.login(email, password);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(password, result.getPassword());
        verify(staffRepository).findByEmail(email);
        verify(staffRepository, never()).findByUsername(anyString());
    }

    @Test
    void testLogin_WithValidUsername_ShouldReturnStaff() {
        String username = "staffuser";
        String password = "password123";
        
        when(staffRepository.findByEmail(username)).thenReturn(Optional.empty());
        when(staffRepository.findByUsername(username)).thenReturn(Optional.of(testStaff));

        Staff result = staffService.login(username, password);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(password, result.getPassword());
        verify(staffRepository).findByEmail(username);
        verify(staffRepository).findByUsername(username);
    }

    @Test
    void testLogin_WithInvalidCredentials_ShouldReturnNull() {
        String username = "wronguser";
        String password = "wrongpass";
        
        when(staffRepository.findByEmail(username)).thenReturn(Optional.empty());
        when(staffRepository.findByUsername(username)).thenReturn(Optional.empty());

        Staff result = staffService.login(username, password);

        assertNull(result);
        verify(staffRepository).findByEmail(username);
        verify(staffRepository).findByUsername(username);
    }

    @Test
    void testLogin_WithWrongPassword_ShouldReturnNull() {
        String email = "staff@test.com";
        String correctPassword = "password123";
        String wrongPassword = "wrongpassword";
        
        testStaff.setPassword(correctPassword);
        when(staffRepository.findByEmail(email)).thenReturn(Optional.of(testStaff));
        when(staffRepository.findByUsername(email)).thenReturn(Optional.empty());

        Staff result = staffService.login(email, wrongPassword);

        assertNull(result);
        verify(staffRepository).findByEmail(email);
        verify(staffRepository).findByUsername(email);
    }
}