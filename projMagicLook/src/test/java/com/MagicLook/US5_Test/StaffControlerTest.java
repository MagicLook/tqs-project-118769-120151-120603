package com.MagicLook.US5_Test;

import com.MagicLook.data.Staff;
import com.MagicLook.data.Shop;
import com.MagicLook.dto.ItemDTO;
import com.MagicLook.service.StaffService;
import com.MagicLook.service.ItemService;
import com.MagicLook.boundary.StaffController;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StaffController.class)
class StaffControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StaffService staffService;

    @MockBean
    private ItemService itemService;

    private MockHttpSession session;
    private Staff staff;
    private Shop shop;

    @BeforeEach
    void setUp() {
        shop = new Shop();
        shop.setShopId(1);
        shop.setName("Loja Centro");

        staff = new Staff();
        staff.setStaffId(UUID.randomUUID());
        staff.setName("Ana Silva");
        staff.setEmail("ana@magiclook.com");
        staff.setUsername("anasi");
        staff.setShop(shop);

        session = new MockHttpSession();
        session.setAttribute("loggedInStaff", staff);
        session.setAttribute("staffId", staff.getStaffId());
        session.setAttribute("shopId", shop.getShopId());
    }

    @Test
    @DisplayName("POST /magiclook/staff/item → Redirect quando item é adicionado com sucesso")
    void shouldRedirectWhenItemIsAddedSuccessfully() throws Exception {
        // Given
        when(staffService.addItem(any(ItemDTO.class))).thenReturn(0);

        // When & Then
        mockMvc.perform(multipart("/magiclook/staff/item")
                        .param("name", "Vestido Azul")
                        .param("brand", "Marca X")
                        .param("material", "Seda")
                        .param("color", "Azul")
                        .param("size", "M")
                        .param("priceRent", "250.00")
                        .param("priceSale", "5000.00")
                        .param("gender", "F")
                        .param("category", "Vestidos")
                        .param("subcategory", "Curto")
                        .param("shop", "1")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/magiclook/staff/dashboard"));

        verify(staffService, times(1)).addItem(any(ItemDTO.class));
    }

    @Test
    @DisplayName("POST /magiclook/staff/item → Erro quando tamanho é inválido (result = -1)")
    void shouldReturnErrorWhenSizeIsInvalid() throws Exception {
        // Given
        when(staffService.addItem(any(ItemDTO.class))).thenReturn(-1);

        // When & Then
        mockMvc.perform(multipart("/magiclook/staff/item")
                        .param("name", "Vestido")
                        .param("brand", "Marca")
                        .param("material", "Seda")
                        .param("color", "Azul")
                        .param("size", "XXXL")
                        .param("priceRent", "250.00")
                        .param("priceSale", "5000.00")
                        .param("gender", "F")
                        .param("category", "Vestidos")
                        .param("subcategory", "Curto")
                        .param("shop", "1")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("staffDashboard"))
                .andExpect(model().attribute("error", "Tamanho inválido!"));

        verify(staffService, times(1)).addItem(any(ItemDTO.class));
    }

    @Test
    @DisplayName("POST /magiclook/staff/item → Erro quando material é inválido (result = -2)")
    void shouldReturnErrorWhenMaterialIsInvalid() throws Exception {
        // Given
        when(staffService.addItem(any(ItemDTO.class))).thenReturn(-2);

        // When & Then
        mockMvc.perform(multipart("/magiclook/staff/item")
                        .param("name", "Vestido")
                        .param("brand", "Marca")
                        .param("material", "Plástico")
                        .param("color", "Azul")
                        .param("size", "M")
                        .param("priceRent", "250.00")
                        .param("priceSale", "5000.00")
                        .param("gender", "F")
                        .param("category", "Vestidos")
                        .param("subcategory", "Curto")
                        .param("shop", "1")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("staffDashboard"))
                .andExpect(model().attribute("error", "Material inválido!"));

        verify(staffService, times(1)).addItem(any(ItemDTO.class));
    }

    @Test
    @DisplayName("POST /magiclook/staff/item → Erro quando shop ou itemType é inválido (result = -3)")
    void shouldReturnErrorWhenShopOrItemTypeIsInvalid() throws Exception {
        // Given
        when(staffService.addItem(any(ItemDTO.class))).thenReturn(-3);

        // When & Then
        mockMvc.perform(multipart("/magiclook/staff/item")
                        .param("name", "Vestido")
                        .param("brand", "Marca")
                        .param("material", "Seda")
                        .param("color", "Azul")
                        .param("size", "M")
                        .param("priceRent", "250.00")
                        .param("priceSale", "5000.00")
                        .param("gender", "F")
                        .param("category", "Categoria Inexistente")
                        .param("subcategory", "Subcategoria Inexistente")
                        .param("shop", "999")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("staffDashboard"))
                .andExpect(model().attribute("error", "Shop ou ItemType inválido!"));

        verify(staffService, times(1)).addItem(any(ItemDTO.class));
    }

    @Test
    @DisplayName("POST /magiclook/staff/item → Upload de imagem com item adicionado")
    void shouldUploadImageWhenItemIsAdded() throws Exception {
        // Given
        MockMultipartFile imageFile = new MockMultipartFile(
            "image",
            "vestido.jpg",
            "image/jpeg",
            "fake image content".getBytes()
        );

        ItemDTO itemDTO = new ItemDTO();
        itemDTO.setItemId(123);

        when(staffService.addItem(any(ItemDTO.class))).thenReturn(0);
        when(staffService.saveImage(any(), eq(123))).thenReturn("uploads/item_123_vestido.jpg");

        // When & Then
        mockMvc.perform(multipart("/magiclook/staff/item")
                        .file(imageFile)
                        .param("name", "Vestido")
                        .param("brand", "Marca")
                        .param("material", "Seda")
                        .param("color", "Azul")
                        .param("size", "M")
                        .param("priceRent", "250.00")
                        .param("priceSale", "5000.00")
                        .param("gender", "F")
                        .param("category", "Vestidos")
                        .param("subcategory", "Curto")
                        .param("shop", "1")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/magiclook/staff/dashboard"));

        verify(staffService, times(1)).addItem(any(ItemDTO.class));
        verify(staffService, times(1)).saveImage(any(), any());
    }

    @Test
    @DisplayName("POST /magiclook/staff/item → Sem upload quando imagem é null")
    void shouldNotUploadWhenImageIsNull() throws Exception {
        // Given
        when(staffService.addItem(any(ItemDTO.class))).thenReturn(0);

        // When & Then
        mockMvc.perform(multipart("/magiclook/staff/item")
                        .param("name", "Vestido")
                        .param("brand", "Marca")
                        .param("material", "Seda")
                        .param("color", "Azul")
                        .param("size", "M")
                        .param("priceRent", "250.00")
                        .param("priceSale", "5000.00")
                        .param("gender", "F")
                        .param("category", "Vestidos")
                        .param("subcategory", "Curto")
                        .param("shop", "1")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/magiclook/staff/dashboard"));

        verify(staffService, times(1)).addItem(any(ItemDTO.class));
        verify(staffService, never()).saveImage(any(), any());
    }

    @Test
    @DisplayName("POST /magiclook/staff/item → Redirect para login quando staff não está logado")
    void shouldRedirectToLoginWhenStaffIsNotLoggedIn() throws Exception {
        // Given - session vazia
        MockHttpSession emptySession = new MockHttpSession();

        // When & Then
        mockMvc.perform(multipart("/magiclook/staff/item")
                        .param("name", "Vestido")
                        .param("brand", "Marca")
                        .param("material", "Seda")
                        .param("color", "Azul")
                        .param("size", "M")
                        .param("priceRent", "250.00")
                        .param("priceSale", "5000.00")
                        .param("gender", "F")
                        .param("category", "Vestidos")
                        .param("subcategory", "Curto")
                        .param("shop", "1")
                        .session(emptySession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/magiclook/staff/login"));

        verify(staffService, never()).addItem(any(ItemDTO.class));
    }

    @Test
    @DisplayName("POST /magiclook/staff/item → Erro quando ocorre exception")
    void shouldHandleExceptionGracefully() throws Exception {
        // Given
        when(staffService.addItem(any(ItemDTO.class))).thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(multipart("/magiclook/staff/item")
                        .param("name", "Vestido")
                        .param("brand", "Marca")
                        .param("material", "Seda")
                        .param("color", "Azul")
                        .param("size", "M")
                        .param("priceRent", "250.00")
                        .param("priceSale", "5000.00")
                        .param("gender", "F")
                        .param("category", "Vestidos")
                        .param("subcategory", "Curto")
                        .param("shop", "1")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("staffDashboard"))
                .andExpect(model().attributeExists("error"));

        verify(staffService, times(1)).addItem(any(ItemDTO.class));
    }

    private ItemDTO createValidItemDTO() {
        return new ItemDTO(
                "Vestido",
                "Seda",
                "Azul",
                "Marca",
                "M",
                new BigDecimal("250.00"),
                new BigDecimal("5000.00"),
                1,   
                "F",
                "Vestidos",
                "Curto"
        );
    }
}