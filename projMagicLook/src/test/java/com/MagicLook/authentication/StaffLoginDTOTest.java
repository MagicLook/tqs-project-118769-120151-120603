package com.MagicLook.authentication;

import com.MagicLook.dto.StaffLoginDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StaffLoginDTOTest {

    @Test
    void testStaffLoginDTO_DefaultConstructor() {
        StaffLoginDTO dto = new StaffLoginDTO();
        assertNotNull(dto);
    }

    @Test
    void testStaffLoginDTO_ArgsConstructor() {
        StaffLoginDTO dto = new StaffLoginDTO("test@email.com", "password123");
        
        assertEquals("test@email.com", dto.getUsernameOrEmail());
        assertEquals("password123", dto.getPassword());
    }

    @Test
    void testStaffLoginDTO_Setters() {
        StaffLoginDTO dto = new StaffLoginDTO();
        dto.setUsernameOrEmail("username");
        dto.setPassword("pass");
        
        assertEquals("username", dto.getUsernameOrEmail());
        assertEquals("pass", dto.getPassword());
    }
}