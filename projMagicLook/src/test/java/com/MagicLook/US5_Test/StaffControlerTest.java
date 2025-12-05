package com.MagicLook.US5_Test;

import com.MagicLook.dto.ItemDTO;
import com.MagicLook.service.StaffService;
import com.MagicLook.boundary.StaffController;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StaffController.class)
class StaffControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;  // Para converter DTO → JSON

    @MockBean
    private StaffService staffService;

    private ItemDTO createValidItemDTO() {
        return new ItemDTO(
                "Vestido",
                "Cetim",
                "Azul",
                "Marca",
                "M",
                new BigDecimal("250.00"),
                new BigDecimal("5000.00"),
                2,   
                1
        );
    }

    @Test
    @DisplayName("POST /magiclook/item → 201 Created quando item é adicionado com sucesso")
    void shouldReturnCreatedWhenItemIsAddedSuccessfully() throws Exception {
        // Given
        when(staffService.addItem(any(ItemDTO.class))).thenReturn(0);

        // When & Then
        mockMvc.perform(post("/magiclook/item")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidItemDTO())))
                .andExpect(status().isCreated())
                .andExpect(content().string("Item adicionado com sucesso."));

        verify(staffService, times(1)).addItem(any(ItemDTO.class));
    }

    @Test
    @DisplayName("POST /magiclook/item → 409 Conflict quando item já existe")
    void shouldReturnConflictWhenItemAlreadyExists() throws Exception {
        // Given
        when(staffService.addItem(any(ItemDTO.class))).thenReturn(-1); 

        // When & Then
        mockMvc.perform(post("/magiclook/item")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidItemDTO())))
                .andExpect(status().isConflict())  // 409
                .andExpect(content().string("Já existe um item com essas características"));

        verify(staffService, times(1)).addItem(any(ItemDTO.class));
    }
}