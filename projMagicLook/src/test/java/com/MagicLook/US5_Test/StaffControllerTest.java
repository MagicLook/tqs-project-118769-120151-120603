package com.magiclook.US5_Test;

import com.magiclook.boundary.StaffController;
import com.magiclook.data.*;
import com.magiclook.dto.ItemDTO;
import com.magiclook.dto.StaffLoginDTO;
import com.magiclook.service.ItemService;
import com.magiclook.service.StaffService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StaffController - Comprehensive Unit Tests")
class StaffControllerTest {

    @Mock
    private StaffService staffService;

    @Mock
    private ItemService itemService;

    @Mock
    private Model model;

    @Mock
    private HttpSession session;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private StaffController staffController;

    private Staff testStaff;
    private Shop testShop;
    private Item testItem;
    private ItemType testItemType;

    @BeforeEach
    void setUp() {
        testShop = new Shop("Test Shop", "Test Location");
        testShop.setShopId(1);

        testStaff = new Staff("Test Staff", "staff@test.com", "password123", "staffuser", testShop);
        testStaff.setStaffId(UUID.randomUUID());

        testItemType = new ItemType("M", "Camiseta", "Manga Curta");
        testItemType.setId(1);

        testItem = new Item("Test Item", "Cotton", "Blue", "Test Brand", 
                           new BigDecimal("50.00"), new BigDecimal("200.00"), testShop, testItemType);
        testItem.setItemId(1);
    }

    // ==================== LOGIN TESTS ====================

    @Nested
    @DisplayName("Login Form Display Tests")
    class LoginFormTests {

        @Test
        @DisplayName("GET /login should display login form with DTO")
        void showStaffLoginForm_shouldReturnLoginView() {
            String viewName = staffController.showStaffLoginForm(model);

            assertEquals("staffLogin", viewName);
            verify(model).addAttribute(eq("staffLogin"), any(StaffLoginDTO.class));
        }
    }

    @Nested
    @DisplayName("Login Authentication Tests")
    class LoginAuthenticationTests {

        @Test
        @DisplayName("POST /login with valid email should authenticate and redirect")
        void staffLogin_withValidEmail_shouldSetSessionAndRedirect() {
            when(staffService.login("staff@test.com", "password123")).thenReturn(testStaff);

            String viewName = staffController.staffLogin("staff@test.com", "password123", session, model);

            assertEquals("redirect:/magiclook/staff/dashboard", viewName);
            verify(session).setAttribute("loggedInStaff", testStaff);
            verify(session).setAttribute("staffId", testStaff.getStaffId());
            verify(session).setAttribute("staffName", testStaff.getName());
            verify(session).setAttribute("staffEmail", testStaff.getEmail());
            verify(session).setAttribute("staffUsername", testStaff.getUsername());
            verify(session).setAttribute("shopId", testShop.getShopId());
            verify(session).setAttribute("shopName", testShop.getName());
            verifyNoInteractions(model);
        }

        @Test
        @DisplayName("POST /login with valid username should authenticate and redirect")
        void staffLogin_withValidUsername_shouldSetSessionAndRedirect() {
            when(staffService.login("staffuser", "password123")).thenReturn(testStaff);

            String viewName = staffController.staffLogin("staffuser", "password123", session, model);

            assertEquals("redirect:/magiclook/staff/dashboard", viewName);
            verify(session, times(7)).setAttribute(anyString(), any());
        }

        @Test
        @DisplayName("POST /login with invalid credentials should show error")
        void staffLogin_withInvalidCredentials_shouldShowError() {
            when(staffService.login("wrong", "wrong")).thenReturn(null);

            String viewName = staffController.staffLogin("wrong", "wrong", session, model);

            assertEquals("staffLogin", viewName);
            verify(model).addAttribute("error", "Credenciais inválidas para staff!");
            verify(session, never()).setAttribute(anyString(), any());
        }

        @Test
        @DisplayName("POST /login with null credentials should show error")
        void staffLogin_withNullCredentials_shouldShowError() {
            when(staffService.login(null, null)).thenReturn(null);

            String viewName = staffController.staffLogin(null, null, session, model);

            assertEquals("staffLogin", viewName);
            verify(model).addAttribute("error", "Credenciais inválidas para staff!");
        }

        @Test
        @DisplayName("POST /login with empty credentials should show error")
        void staffLogin_withEmptyCredentials_shouldShowError() {
            when(staffService.login("", "")).thenReturn(null);

            String viewName = staffController.staffLogin("", "", session, model);

            assertEquals("staffLogin", viewName);
            verify(model).addAttribute("error", "Credenciais inválidas para staff!");
        }

        @Test
        @DisplayName("POST /login with wrong password should show error")
        void staffLogin_withWrongPassword_shouldShowError() {
            when(staffService.login("staff@test.com", "wrongpassword")).thenReturn(null);

            String viewName = staffController.staffLogin("staff@test.com", "wrongpassword", session, model);

            assertEquals("staffLogin", viewName);
            verify(model).addAttribute("error", "Credenciais inválidas para staff!");
        }
    }

    // ==================== DASHBOARD TESTS ====================

    @Nested
    @DisplayName("Dashboard Display Tests")
    class DashboardTests {

        @Test
        @DisplayName("GET /dashboard with logged staff should show dashboard with items")
        void showStaffDashboard_withLoggedInStaff_shouldReturnDashboard() {
            List<Item> items = Arrays.asList(testItem);
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);
            when(itemService.getItemsByShop(testShop)).thenReturn(items);

            String viewName = staffController.showStaffDashboard(session, model);

            assertEquals("staffDashboard", viewName);
            verify(model).addAttribute("staff", testStaff);
            verify(model).addAttribute("shop", testShop);
            verify(model).addAttribute("items", items);
            verify(model).addAttribute("itemCount", 1);
            verify(itemService).getItemsByShop(testShop);
        }

        @Test
        @DisplayName("GET /dashboard without authentication should redirect to login")
        void showStaffDashboard_withoutLoggedInStaff_shouldRedirectToLogin() {
            when(session.getAttribute("loggedInStaff")).thenReturn(null);

            String viewName = staffController.showStaffDashboard(session, model);

            assertEquals("redirect:/magiclook/staff/login", viewName);
            verify(model, never()).addAttribute(anyString(), any());
            verifyNoInteractions(itemService);
        }

        @Test
        @DisplayName("GET /dashboard with empty items should show zero count")
        void showStaffDashboard_withNoItems_shouldShowZeroCount() {
            List<Item> emptyList = Collections.emptyList();
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);
            when(itemService.getItemsByShop(testShop)).thenReturn(emptyList);

            String viewName = staffController.showStaffDashboard(session, model);

            assertEquals("staffDashboard", viewName);
            verify(model).addAttribute("items", emptyList);
            verify(model).addAttribute("itemCount", 0);
        }

        @Test
        @DisplayName("GET /dashboard with multiple items should show correct count")
        void showStaffDashboard_withMultipleItems_shouldShowCorrectCount() {
            List<Item> items = Arrays.asList(testItem, testItem, testItem);
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);
            when(itemService.getItemsByShop(testShop)).thenReturn(items);

            String viewName = staffController.showStaffDashboard(session, model);

            verify(model).addAttribute("itemCount", 3);
        }

        @Test
        @DisplayName("Dashboard with staff having null shop should call itemService with null")
        void showStaffDashboard_withNullShop_shouldCallItemServiceWithNull() {
            Staff staffWithoutShop = new Staff();
            staffWithoutShop.setStaffId(UUID.randomUUID());
            when(session.getAttribute("loggedInStaff")).thenReturn(staffWithoutShop);
            when(itemService.getItemsByShop(null)).thenReturn(Collections.emptyList());

            String viewName = staffController.showStaffDashboard(session, model);

            assertEquals("staffDashboard", viewName);
            verify(itemService).getItemsByShop(null);
            verify(model).addAttribute("staff", staffWithoutShop);
            verify(model).addAttribute("shop", null);
        }
    }

    // ==================== ADD ITEM TESTS ====================

    @Nested
    @DisplayName("Add Item Tests")
    class AddItemTests {

        @Test
        @DisplayName("POST /item without authentication should redirect to login")
        void addItem_withoutAuthentication_shouldRedirectToLogin() {
            when(session.getAttribute("loggedInStaff")).thenReturn(null);

            String viewName = staffController.addItem(
                "Item", "Brand", "Cotton", "Blue", "M",
                new BigDecimal("50.00"), new BigDecimal("200.00"),
                "M", "Camiseta", "Manga Curta", 1, null,
                session, model
            );

            assertEquals("redirect:/magiclook/staff/login", viewName);
            verifyNoInteractions(staffService);
        }

        @Test
        @DisplayName("POST /item with valid data should create item and redirect")
        void addItem_withValidData_shouldCreateItemAndRedirect() {
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);
            when(staffService.addItem(any(ItemDTO.class), eq("M"))).thenReturn(0);

            String viewName = staffController.addItem(
                "Test Item", "Test Brand", "Cotton", "Blue", "M",
                new BigDecimal("50.00"), new BigDecimal("200.00"),
                "M", "Camiseta", "Manga Curta", 1, null,
                session, model
            );

            assertEquals("redirect:/magiclook/staff/dashboard", viewName);
            verify(staffService).addItem(any(ItemDTO.class), eq("M"));
            verify(model, never()).addAttribute(eq("error"), anyString());
        }

        @Test
        @DisplayName("POST /item with invalid size should show error")
        void addItem_withInvalidSize_shouldShowError() {
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);
            when(staffService.addItem(any(ItemDTO.class), eq("XXXL"))).thenReturn(-1);

            String viewName = staffController.addItem(
                "Test Item", "Test Brand", "Cotton", "Blue", "XXXL",
                new BigDecimal("50.00"), new BigDecimal("200.00"),
                "M", "Camiseta", "Manga Curta", 1, null,
                session, model
            );

            assertEquals("staffDashboard", viewName);
            verify(model).addAttribute("error", "Tamanho inválido!");
        }

        @Test
        @DisplayName("POST /item with invalid material should show error")
        void addItem_withInvalidMaterial_shouldShowError() {
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);
            when(staffService.addItem(any(ItemDTO.class), eq("M"))).thenReturn(-2);

            String viewName = staffController.addItem(
                "Test Item", "Test Brand", "InvalidMaterial", "Blue", "M",
                new BigDecimal("50.00"), new BigDecimal("200.00"),
                "M", "Camiseta", "Manga Curta", 1, null,
                session, model
            );

            assertEquals("staffDashboard", viewName);
            verify(model).addAttribute("error", "Material inválido!");
        }

        @Test
        @DisplayName("POST /item with invalid shop or item type should show error")
        void addItem_withInvalidShopOrItemType_shouldShowError() {
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);
            when(staffService.addItem(any(ItemDTO.class), eq("M"))).thenReturn(-3);

            String viewName = staffController.addItem(
                "Test Item", "Test Brand", "Cotton", "Blue", "M",
                new BigDecimal("50.00"), new BigDecimal("200.00"),
                "M", "InvalidCategory", "InvalidSubcategory", 999, null,
                session, model
            );

            assertEquals("staffDashboard", viewName);
            verify(model).addAttribute("error", "Shop ou ItemType inválido!");
        }

        @Test
        @DisplayName("POST /item with image should save image")
        void addItem_withImage_shouldSaveImage() throws IOException {
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);
            when(staffService.addItem(any(ItemDTO.class), eq("M"))).thenReturn(0);
            when(multipartFile.isEmpty()).thenReturn(false);
            when(staffService.saveImage(any(MultipartFile.class), any())).thenReturn("/uploads/item_123_test.jpg");

            String viewName = staffController.addItem(
                "Test Item", "Test Brand", "Cotton", "Blue", "M",
                new BigDecimal("50.00"), new BigDecimal("200.00"),
                "M", "Camiseta", "Manga Curta", 1, multipartFile,
                session, model
            );

            assertEquals("redirect:/magiclook/staff/dashboard", viewName);
            verify(staffService).saveImage(eq(multipartFile), any());
        }

        @Test
        @DisplayName("POST /item with null image should not save image")
        void addItem_withNullImage_shouldNotSaveImage() throws IOException {
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);
            when(staffService.addItem(any(ItemDTO.class), eq("M"))).thenReturn(0);

            String viewName = staffController.addItem(
                "Test Item", "Test Brand", "Cotton", "Blue", "M",
                new BigDecimal("50.00"), new BigDecimal("200.00"),
                "M", "Camiseta", "Manga Curta", 1, null,
                session, model
            );

            assertEquals("redirect:/magiclook/staff/dashboard", viewName);
            verify(staffService, never()).saveImage(any(), any());
        }

        @Test
        @DisplayName("POST /item with empty image should not save image")
        void addItem_withEmptyImage_shouldNotSaveImage() throws IOException {
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);
            when(staffService.addItem(any(ItemDTO.class), eq("M"))).thenReturn(0);
            when(multipartFile.isEmpty()).thenReturn(true);

            String viewName = staffController.addItem(
                "Test Item", "Test Brand", "Cotton", "Blue", "M",
                new BigDecimal("50.00"), new BigDecimal("200.00"),
                "M", "Camiseta", "Manga Curta", 1, multipartFile,
                session, model
            );

            assertEquals("redirect:/magiclook/staff/dashboard", viewName);
            verify(staffService, never()).saveImage(any(), any());
        }

        @Test
        @DisplayName("POST /item with exception should show error message")
        void addItem_withException_shouldShowError() {
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);
            when(staffService.addItem(any(ItemDTO.class), eq("M")))
                .thenThrow(new RuntimeException("Database error"));

            String viewName = staffController.addItem(
                "Test Item", "Test Brand", "Cotton", "Blue", "M",
                new BigDecimal("50.00"), new BigDecimal("200.00"),
                "M", "Camiseta", "Manga Curta", 1, null,
                session, model
            );

            assertEquals("staffDashboard", viewName);
            verify(model).addAttribute(eq("error"), contains("Erro ao adicionar item"));
        }

        @Test
        @DisplayName("POST /item should create ItemDTO with correct parameters")
        void addItem_shouldCreateItemDTOWithCorrectParameters() {
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);
            when(staffService.addItem(any(ItemDTO.class), eq("L"))).thenReturn(0);

            staffController.addItem(
                "Vestido", "Zara", "Seda", "Vermelho", "L",
                new BigDecimal("100.00"), new BigDecimal("500.00"),
                "F", "Vestido", "Longo", 2, null,
                session, model
            );

            verify(staffService).addItem(argThat(dto ->
                dto.getName().equals("Vestido") &&
                dto.getBrand().equals("Zara") &&
                dto.getMaterial().equals("Seda") &&
                dto.getColor().equals("Vermelho") &&
                dto.getPriceRent().equals(new BigDecimal("100.00")) &&
                dto.getPriceSale().equals(new BigDecimal("500.00")) &&
                dto.getShopId().equals(2) &&
                dto.getGender().equals("F") &&
                dto.getCategory().equals("Vestido") &&
                dto.getSubcategory().equals("Longo")
            ), eq("L"));
        }
    }

    // ==================== LOGOUT TESTS ====================

    @Nested
    @DisplayName("Logout Tests")
    class LogoutTests {

        @Test
        @DisplayName("GET /logout should clear session and redirect")
        void staffLogout_shouldClearSessionAndRedirect() {
            String viewName = staffController.staffLogout(session);

            assertEquals("redirect:/magiclook/staff/login?logout", viewName);
            verify(session).removeAttribute("loggedInStaff");
            verify(session).removeAttribute("staffId");
            verify(session).removeAttribute("staffName");
            verify(session).removeAttribute("staffEmail");
            verify(session).removeAttribute("staffUsername");
            verify(session).removeAttribute("shopId");
            verify(session).removeAttribute("shopName");
        }

        @Test
        @DisplayName("GET /logout with null session should still redirect")
        void staffLogout_withNullSession_shouldStillRedirect() {
            String viewName = staffController.staffLogout(null);

            assertEquals("redirect:/magiclook/staff/login?logout", viewName);
        }

        @Test
        @DisplayName("GET /logout should remove all 7 session attributes")
        void staffLogout_shouldRemoveAllSessionAttributes() {
            staffController.staffLogout(session);

            verify(session, times(7)).removeAttribute(anyString());
        }
    }

    // ==================== EDGE CASE TESTS ====================

    @Nested
    @DisplayName("Edge Case and Integration Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Add item with decimal prices should handle correctly")
        void addItem_withDecimalPrices_shouldHandleCorrectly() {
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);
            when(staffService.addItem(any(ItemDTO.class), eq("M"))).thenReturn(0);

            staffController.addItem(
                "Item", "Brand", "Cotton", "Blue", "M",
                new BigDecimal("99.99"), new BigDecimal("499.99"),
                "M", "Camiseta", "Manga Curta", 1, null,
                session, model
            );

            verify(staffService).addItem(argThat(dto ->
                dto.getPriceRent().equals(new BigDecimal("99.99")) &&
                dto.getPriceSale().equals(new BigDecimal("499.99"))
            ), eq("M"));
        }

        @Test
        @DisplayName("Add item with special characters in name should work")
        void addItem_withSpecialCharactersInName_shouldWork() {
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);
            when(staffService.addItem(any(ItemDTO.class), eq("M"))).thenReturn(0);

            staffController.addItem(
                "Item & Co. #1", "Brand's", "Cotton", "Blue", "M",
                new BigDecimal("50.00"), new BigDecimal("200.00"),
                "M", "Camiseta", "Manga Curta", 1, null,
                session, model
            );

            verify(staffService).addItem(argThat(dto ->
                dto.getName().equals("Item & Co. #1") &&
                dto.getBrand().equals("Brand's")
            ), eq("M"));
        }
    }
}
