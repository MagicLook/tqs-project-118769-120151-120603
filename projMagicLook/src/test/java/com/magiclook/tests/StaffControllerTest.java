package com.magiclook.tests;

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

            assertEquals("redirect:/magiclook/staff/item", viewName);
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

            assertEquals("redirect:/magiclook/staff/item", viewName);
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
        @DisplayName("GET /dashboard with logged staff should redirect to items page")
        void showStaffDashboard_withLoggedInStaff_shouldReturnDashboard() {
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);

            String viewName = staffController.showStaffDashboard(session, model);

            assertEquals("redirect:/magiclook/staff/item", viewName);
            verifyNoInteractions(model);
            verifyNoInteractions(itemService);
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
        @DisplayName("GET /dashboard with empty items should redirect to items page")
        void showStaffDashboard_withNoItems_shouldShowZeroCount() {
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);

            String viewName = staffController.showStaffDashboard(session, model);

            assertEquals("redirect:/magiclook/staff/item", viewName);
            verifyNoInteractions(model);
        }

        @Test
        @DisplayName("GET /dashboard with multiple items should redirect to items page")
        void showStaffDashboard_withMultipleItems_shouldShowCorrectCount() {
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);

            String viewName = staffController.showStaffDashboard(session, model);

            assertEquals("redirect:/magiclook/staff/item", viewName);
            verifyNoInteractions(model);
        }

        @Test
        @DisplayName("Dashboard with staff having null shop should redirect to items page")
        void showStaffDashboard_withNullShop_shouldCallItemServiceWithNull() {
            Staff staffWithoutShop = new Staff();
            staffWithoutShop.setStaffId(UUID.randomUUID());
            when(session.getAttribute("loggedInStaff")).thenReturn(staffWithoutShop);

            String viewName = staffController.showStaffDashboard(session, model);

            assertEquals("redirect:/magiclook/staff/item", viewName);
            verifyNoInteractions(model);
            verifyNoInteractions(itemService);
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

            assertEquals("redirect:/magiclook/staff/item", viewName);
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

            assertEquals("redirect:/magiclook/staff/item", viewName);
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

            assertEquals("redirect:/magiclook/staff/item", viewName);
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

            assertEquals("redirect:/magiclook/staff/item", viewName);
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

    // ==================== GET ITEMS TESTS ====================

    @Nested
    @DisplayName("Get Items (List View) Tests")
    class GetItemsTests {

        @Test
        @DisplayName("GET /item without authentication should redirect to login")
        void getItems_withoutAuthentication_shouldRedirectToLogin() {
            when(session.getAttribute("loggedInStaff")).thenReturn(null);

            String viewName = staffController.getItems(session, model, null, null);

            assertEquals("redirect:/magiclook/staff/login", viewName);
            verifyNoInteractions(itemService);
        }

        @Test
        @DisplayName("GET /item with logged staff should return items page")
        void getItems_withLoggedStaff_shouldReturnItemsPage() {
            List<Item> items = Arrays.asList(testItem);
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);
            when(itemService.getItemsByShop(testShop)).thenReturn(items);
            when(itemService.getItems(testItem.getItemId())).thenReturn(new ArrayList<>());

            String viewName = staffController.getItems(session, model, null, null);

            assertEquals("staffItem", viewName);
            verify(model).addAttribute("staff", testStaff);
            verify(model).addAttribute("shop", testShop);
            verify(model).addAttribute("items", items);
            verify(model).addAttribute("itemCount", 1);
        }

        @Test
        @DisplayName("GET /item with empty items list should show zero count")
        void getItems_withEmptyItemsList_shouldShowZeroCount() {
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);
            when(itemService.getItemsByShop(testShop)).thenReturn(Collections.emptyList());

            String viewName = staffController.getItems(session, model, null, null);

            assertEquals("staffItem", viewName);
            verify(model).addAttribute("itemCount", 0);
            verify(model).addAttribute("items", Collections.emptyList());
        }

        @Test
        @DisplayName("GET /item with search query should filter items by name")
        void getItems_withSearchQuery_shouldFilterByName() {
            Item item2 = new Item("Blue Shirt", "Cotton", "Blue", "Brand", 
                                 new BigDecimal("40.00"), new BigDecimal("150.00"), testShop, testItemType);
            item2.setItemId(2);
            
            List<Item> allItems = Arrays.asList(testItem, item2);
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);
            when(itemService.getItemsByShop(testShop)).thenReturn(allItems);
            when(itemService.getItems(anyInt())).thenReturn(new ArrayList<>());

            String viewName = staffController.getItems(session, model, null, "Blue");

            assertEquals("staffItem", viewName);
            verify(model).addAttribute("q", "Blue");
            verify(itemService).getItemsByShop(testShop);
        }

        @Test
        @DisplayName("GET /item with state filter should filter by item state")
        void getItems_withStateFilter_shouldFilterByState() {
            List<Item> allItems = Arrays.asList(testItem);
            List<Item> filteredItems = Arrays.asList(testItem);
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);
            when(itemService.getItemsByShop(testShop)).thenReturn(allItems);
            when(itemService.getAllItemsByState("AVAILABLE")).thenReturn(filteredItems);
            when(itemService.getItems(testItem.getItemId())).thenReturn(new ArrayList<>());

            String viewName = staffController.getItems(session, model, "AVAILABLE", null);

            assertEquals("staffItem", viewName);
            verify(model).addAttribute("selectedState", "AVAILABLE");
            verify(itemService).getAllItemsByState("AVAILABLE");
        }

        @Test
        @DisplayName("GET /item with both search and state filter should apply both")
        void getItems_withSearchAndStateFilter_shouldApplyBoth() {
            testItem.setName("Test Item");
            List<Item> allItems = Arrays.asList(testItem);
            List<Item> filteredItems = Arrays.asList(testItem);
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);
            when(itemService.getItemsByShop(testShop)).thenReturn(allItems);
            when(itemService.getAllItemsByState("RENTED")).thenReturn(filteredItems);
            when(itemService.getItems(testItem.getItemId())).thenReturn(new ArrayList<>());

            String viewName = staffController.getItems(session, model, "RENTED", "Test");

            assertEquals("staffItem", viewName);
            verify(model).addAttribute("q", "Test");
            verify(model).addAttribute("selectedState", "RENTED");
        }

        @Test
        @DisplayName("GET /item should populate itemSizes map for each item")
        void getItems_shouldPopulateItemSizesMap() {
            ItemSingle single1 = new ItemSingle("AVAILABLE", testItem, "M");
            ItemSingle single2 = new ItemSingle("AVAILABLE", testItem, "L");
            List<ItemSingle> singles = Arrays.asList(single1, single2);
            
            List<Item> items = Arrays.asList(testItem);
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);
            when(itemService.getItemsByShop(testShop)).thenReturn(items);
            when(itemService.getItems(testItem.getItemId())).thenReturn(singles);

            String viewName = staffController.getItems(session, model, null, null);

            assertEquals("staffItem", viewName);
            verify(model).addAttribute(eq("itemSizes"), argThat(map ->
                ((Map<Integer, List<String>>) map).containsKey(testItem.getItemId()) &&
                ((Map<Integer, List<String>>) map).get(testItem.getItemId()).containsAll(Arrays.asList("L", "M"))
            ));
        }

        @Test
        @DisplayName("GET /item with multiple items should create sizes map for all")
        void getItems_withMultipleItems_shouldCreateSizesForAll() {
            Item item2 = new Item("Pants", "Cotton", "Black", "Brand", 
                                 new BigDecimal("60.00"), new BigDecimal("250.00"), testShop, testItemType);
            item2.setItemId(2);
            
            ItemSingle single1 = new ItemSingle("AVAILABLE", testItem, "M");
            ItemSingle single2 = new ItemSingle("AVAILABLE", item2, "S");
            
            List<Item> items = Arrays.asList(testItem, item2);
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);
            when(itemService.getItemsByShop(testShop)).thenReturn(items);
            when(itemService.getItems(testItem.getItemId())).thenReturn(Arrays.asList(single1));
            when(itemService.getItems(item2.getItemId())).thenReturn(Arrays.asList(single2));

            String viewName = staffController.getItems(session, model, null, null);

            assertEquals("staffItem", viewName);
            verify(model).addAttribute(eq("itemSizes"), argThat(map ->
                ((Map<Integer, List<String>>) map).size() == 2
            ));
        }

        @Test
        @DisplayName("GET /item with case-insensitive search should find items")
        void getItems_withCaseInsensitiveSearch_shouldFindItems() {
            List<Item> allItems = Arrays.asList(testItem);
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);
            when(itemService.getItemsByShop(testShop)).thenReturn(allItems);
            when(itemService.getItems(anyInt())).thenReturn(new ArrayList<>());

            String viewName = staffController.getItems(session, model, null, "TEST");

            assertEquals("staffItem", viewName);
            verify(model).addAttribute("q", "TEST");
        }

        @Test
        @DisplayName("GET /item with blank search should not filter")
        void getItems_withBlankSearch_shouldNotFilter() {
            List<Item> items = Arrays.asList(testItem);
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);
            when(itemService.getItemsByShop(testShop)).thenReturn(items);
            when(itemService.getItems(testItem.getItemId())).thenReturn(new ArrayList<>());

            String viewName = staffController.getItems(session, model, null, "   ");

            assertEquals("staffItem", viewName);
        }

        @Test
        @DisplayName("GET /item with blank state should not filter by state")
        void getItems_withBlankState_shouldNotFilterByState() {
            List<Item> items = Arrays.asList(testItem);
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);
            when(itemService.getItemsByShop(testShop)).thenReturn(items);
            when(itemService.getItems(testItem.getItemId())).thenReturn(new ArrayList<>());

            String viewName = staffController.getItems(session, model, "   ", null);

            assertEquals("staffItem", viewName);
            verify(itemService, never()).getAllItemsByState(anyString());
        }
    }

    // ==================== GET ITEM DETAILS TESTS ====================

    @Nested
    @DisplayName("Get Item Details Tests")
    class GetItemDetailsTests {

        @Test
        @DisplayName("GET /item/{id} without authentication should redirect to login")
        void getItemDetails_withoutAuthentication_shouldRedirectToLogin() {
            when(session.getAttribute("loggedInStaff")).thenReturn(null);

            String viewName = staffController.getItemDetails(1, session, model);

            assertEquals("redirect:/magiclook/staff/login", viewName);
            verifyNoInteractions(itemService);
        }

        @Test
        @DisplayName("GET /item/{id} with logged staff should return details view")
        void getItemDetails_withLoggedStaff_shouldReturnDetailsView() {
            List<ItemSingle> singles = Arrays.asList(
                new ItemSingle("AVAILABLE", testItem, "M"),
                new ItemSingle("AVAILABLE", testItem, "L")
            );
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);
            when(itemService.getItemById(1)).thenReturn(Optional.of(testItem));
            when(itemService.getItems(1)).thenReturn(singles);

            String viewName = staffController.getItemDetails(1, session, model);

            assertEquals("staffItemDetails", viewName);
            verify(model).addAttribute("staff", testStaff);
            verify(model).addAttribute("shop", testShop);
            verify(model).addAttribute("item", testItem);
            verify(model).addAttribute("itemSingles", singles);
        }

        @Test
        @DisplayName("GET /item/{id} should fetch correct item by ID")
        void getItemDetails_shouldFetchItemById() {
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);
            when(itemService.getItemById(42)).thenReturn(Optional.of(testItem));
            when(itemService.getItems(42)).thenReturn(Collections.emptyList());

            String viewName = staffController.getItemDetails(42, session, model);

            assertEquals("staffItemDetails", viewName);
            verify(itemService).getItemById(42);
        }

        @Test
        @DisplayName("GET /item/{id} with no item singles should show empty list")
        void getItemDetails_withNoItemSingles_shouldShowEmptyList() {
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);
            when(itemService.getItemById(1)).thenReturn(Optional.of(testItem));
            when(itemService.getItems(1)).thenReturn(Collections.emptyList());

            String viewName = staffController.getItemDetails(1, session, model);

            assertEquals("staffItemDetails", viewName);
            verify(model).addAttribute("itemSingles", Collections.emptyList());
        }

        @Test
        @DisplayName("GET /item/{id} with multiple item singles should display all")
        void getItemDetails_withMultipleItemSingles_shouldDisplayAll() {
            List<ItemSingle> singles = Arrays.asList(
                new ItemSingle("AVAILABLE", testItem, "XS"),
                new ItemSingle("AVAILABLE", testItem, "S"),
                new ItemSingle("RENTED", testItem, "M"),
                new ItemSingle("DAMAGED", testItem, "L"),
                new ItemSingle("LAUNDRY", testItem, "XL")
            );
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);
            when(itemService.getItemById(1)).thenReturn(Optional.of(testItem));
            when(itemService.getItems(1)).thenReturn(singles);

            String viewName = staffController.getItemDetails(1, session, model);

            assertEquals("staffItemDetails", viewName);
            verify(model).addAttribute("itemSingles", singles);
            verify(itemService).getItems(1);
        }

        @Test
        @DisplayName("GET /item/{id} should pass staff and shop to model")
        void getItemDetails_shouldPassStaffAndShopToModel() {
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);
            when(itemService.getItemById(1)).thenReturn(Optional.of(testItem));
            when(itemService.getItems(1)).thenReturn(new ArrayList<>());

            staffController.getItemDetails(1, session, model);

            verify(model).addAttribute("staff", testStaff);
            verify(model).addAttribute("shop", testStaff.getShop());
        }

        @Test
        @DisplayName("GET /item/{id} with different items should fetch correct item")
        void getItemDetails_withDifferentItems_shouldFetchCorrectOne() {
            Item item1 = new Item("Item 1", "Cotton", "Red", "Brand", 
                                 new BigDecimal("50.00"), new BigDecimal("200.00"), testShop, testItemType);
            item1.setItemId(1);
            
            Item item2 = new Item("Item 2", "Silk", "Blue", "Brand", 
                                 new BigDecimal("100.00"), new BigDecimal("400.00"), testShop, testItemType);
            item2.setItemId(2);
            
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);
            when(itemService.getItemById(2)).thenReturn(Optional.of(item2));
            when(itemService.getItems(2)).thenReturn(Collections.emptyList());

            String viewName = staffController.getItemDetails(2, session, model);

            assertEquals("staffItemDetails", viewName);
            verify(model).addAttribute("item", item2);
            verify(itemService).getItemById(2);
        }

        @Test
        @DisplayName("GET /item/{id} should call itemService.getItems with correct item ID")
        void getItemDetails_shouldCallGetItemsWithCorrectId() {
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);
            when(itemService.getItemById(99)).thenReturn(Optional.of(testItem));
            when(itemService.getItems(99)).thenReturn(Collections.emptyList());

            staffController.getItemDetails(99, session, model);

            verify(itemService).getItems(99);
        }

        @Test
        @DisplayName("GET /item/{id} with item singles of different states should display all states")
        void getItemDetails_withDifferentStates_shouldDisplayAll() {
            List<ItemSingle> singles = Arrays.asList(
                new ItemSingle("AVAILABLE", testItem, "M"),
                new ItemSingle("RENTED", testItem, "L"),
                new ItemSingle("LAUNDRY", testItem, "XL")
            );
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);
            when(itemService.getItemById(1)).thenReturn(Optional.of(testItem));
            when(itemService.getItems(1)).thenReturn(singles);

            String viewName = staffController.getItemDetails(1, session, model);

            assertEquals("staffItemDetails", viewName);
            verify(model).addAttribute(eq("itemSingles"), argThat(list ->
                ((List<?>) list).size() == 3
            ));
        }

        @Test
        @DisplayName("GET /item/{id} should populate all required model attributes")
        void getItemDetails_shouldPopulateAllModelAttributes() {
            List<ItemSingle> singles = Arrays.asList(new ItemSingle("AVAILABLE", testItem, "M"));
            when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);
            when(itemService.getItemById(1)).thenReturn(Optional.of(testItem));
            when(itemService.getItems(1)).thenReturn(singles);

            staffController.getItemDetails(1, session, model);

            verify(model, times(4)).addAttribute(anyString(), any());
            verify(model).addAttribute("staff", testStaff);
            verify(model).addAttribute("shop", testShop);
            verify(model).addAttribute("item", testItem);
            verify(model).addAttribute("itemSingles", singles);
        }
    }
}
