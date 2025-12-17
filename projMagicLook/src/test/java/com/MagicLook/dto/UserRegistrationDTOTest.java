package com.magiclook.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserRegistrationDTOTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setup() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    private UserRegistrationDTO validDTO() {
        UserRegistrationDTO dto = new UserRegistrationDTO();
        dto.setUsername("testuser");
        dto.setEmail("test@example.com");
        dto.setPassword("password123");
        dto.setFirstName("Ana");
        dto.setLastName("Silva");
        dto.setTelephone("912345678");
        return dto;
    }

    @Test
    void validDTO_ShouldHaveNoViolations() {
        UserRegistrationDTO dto = validDTO();

        Set<ConstraintViolation<UserRegistrationDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), () -> "Unexpected violations: " + violations);
    }

    @Test
    void firstName_TooShort_ShouldViolateSize() {
        UserRegistrationDTO dto = validDTO();
        dto.setFirstName("A");

        Set<ConstraintViolation<UserRegistrationDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Deve haver violações para nome muito curto");
        
        boolean hasSizeViolation = violations.stream()
            .anyMatch(v -> v.getMessage().contains("2-50") || 
                        v.getMessage().contains("pelo menos 2") ||
                        v.getMessage().toLowerCase().contains("size") ||
                        v.getPropertyPath().toString().equals("firstName"));
        
        assertTrue(hasSizeViolation, "Deve haver violação de tamanho para firstName");
    }

    @Test
    void lastName_InvalidCharacters_ShouldViolatePattern() {
        UserRegistrationDTO dto = validDTO();
        dto.setLastName("Silv4");

        Set<ConstraintViolation<UserRegistrationDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().toLowerCase().contains("letras") || v.getMessage().toLowerCase().contains("hífen")));
    }

    @Test
    void telephone_InvalidLength_ShouldViolatePattern() {
        UserRegistrationDTO dto = validDTO();
        dto.setTelephone("12345");

        Set<ConstraintViolation<UserRegistrationDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().toLowerCase().contains("9 dígitos")));
    }

    @Test
    void password_TooShort_ShouldViolateSize() {
        UserRegistrationDTO dto = validDTO();
        dto.setPassword("12345");

        Set<ConstraintViolation<UserRegistrationDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().toLowerCase().contains("pelo menos 6") || v.getMessage().toLowerCase().contains("pelo menos 6 caracteres")));
    }
}
