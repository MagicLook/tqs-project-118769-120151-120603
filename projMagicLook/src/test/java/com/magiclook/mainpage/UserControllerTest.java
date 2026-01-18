package com.magiclook.mainpage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.magiclook.dto.ItemFilterDTO;
import com.magiclook.boundary.UserController;
import com.magiclook.data.User;
import com.magiclook.data.Item;
import com.magiclook.service.UserService;
import com.magiclook.service.ItemService;
import com.magiclook.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.springframework.mock.web.MockHttpSession;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class UserControllerTest {

    private UserController userController;

    @Mock
    private UserService userService;

    @Mock
    private ItemService itemService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private Model model;

    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userController = new UserController(userService, itemService, notificationRepository);
        session = new MockHttpSession();
    }

    @Test
    void testDashboard_WithLoggedInUser_ShouldReturnDashboard() {
        User user = new User();
        user.setUsername("testuser");
        session.setAttribute("loggedInUser", user);

        when(itemService.getRecentItems(6)).thenReturn(new ArrayList<>());

        String viewName = userController.showDashboard(session, model);

        assertEquals("dashboard", viewName);
        verify(model).addAttribute("user", user);
        verify(model).addAttribute("recentItems", new ArrayList<>());
        verify(model).addAttribute("activePage", "dashboard");
    }

    @Test
    void testDashboard_WithoutLoggedInUser_ShouldRedirectToLogin() {

        String viewName = userController.showDashboard(session, model);

        assertEquals("redirect:/magiclook/login", viewName);
    }

    @Test
    void testMenItems_WithLoggedInUser_ShouldReturnMenItemsPage() {
        User user = new User();
        user.setUsername("testuser");
        session.setAttribute("loggedInUser", user);

        List<Item> menItems = createTestItems("M", 3);
        when(itemService.getItemsByGender("M")).thenReturn(menItems);

        String viewName = userController.showMenItems(session, model);

        assertEquals("items/men", viewName);
        verify(model).addAttribute("user", user);
        verify(model).addAttribute("items", menItems);
        verify(model).addAttribute("itemCount", 3);
        verify(model).addAttribute("activePage", "men");
    }

    @Test
    void testMenItems_WithoutLoggedInUser_ShouldRedirectToLogin() {

        String viewName = userController.showMenItems(session, model);

        assertEquals("redirect:/magiclook/login", viewName);
    }

    @Test
    void testWomenItems_WithLoggedInUser_ShouldReturnWomenItemsPage() {
        User user = new User();
        user.setUsername("testuser");
        session.setAttribute("loggedInUser", user);

        List<Item> womenItems = createTestItems("F", 2);
        when(itemService.getItemsByGender("F")).thenReturn(womenItems);

        String viewName = userController.showWomenItems(session, model);

        assertEquals("items/women", viewName);
        verify(model).addAttribute("user", user);
        verify(model).addAttribute("items", womenItems);
        verify(model).addAttribute("itemCount", 2);
        verify(model).addAttribute("activePage", "women");
    }

    @Test
    void testWomenItems_WithoutLoggedInUser_ShouldRedirectToLogin() {

        String viewName = userController.showWomenItems(session, model);

        assertEquals("redirect:/magiclook/login", viewName);
    }

    @Test
    void testShowMenItems_WithFilters_PopulatesModelAndReturnsView() {
        User user = new User();
        user.setUsername("testuser");
        session.setAttribute("loggedInUser", user);

        // prepare filter params
        String color = "Blue";
        String brand = "BrandX";
        String material = "Silk";
        String category = "Shirt";
        String subcategory = "Casual";
        Double minPrice = 10.0;
        Double maxPrice = 50.0;
        String shopLocation = "Lisbon";
        String size = "M";

        List<Item> filteredItems = createTestItems("M", 2);
        when(itemService.findByGenderAndFilters(eq("M"), any())).thenReturn(filteredItems);
        when(itemService.getAllDistinctColors()).thenReturn(List.of("Blue"));
        when(itemService.getAllDistinctBrands()).thenReturn(List.of("BrandX"));
        when(itemService.getAllDistinctMaterials()).thenReturn(List.of("Silk"));
        when(itemService.getAllDistinctCategories()).thenReturn(List.of("Shirt"));
        when(itemService.getAllDistinctSubcategoriesByGender("M")).thenReturn(List.of("Casual"));
        when(itemService.getAllDistinctSizesByGender("M")).thenReturn(List.of("M"));
        when(itemService.getAllDistinctShopLocations()).thenReturn(List.of("Lisbon"));

        String viewName = userController.showMenItems(color, brand, material, category, subcategory, minPrice, maxPrice, shopLocation, size, session, model);

        assertEquals("items/men", viewName);

        // capture filter passed into model - verify called exactly once
        @SuppressWarnings("unchecked")
        org.mockito.ArgumentCaptor<com.magiclook.dto.ItemFilterDTO> captor = org.mockito.ArgumentCaptor.forClass(com.magiclook.dto.ItemFilterDTO.class);
        verify(model, times(1)).addAttribute(eq("filter"), captor.capture());
        com.magiclook.dto.ItemFilterDTO captured = captor.getValue();
        assertEquals(color, captured.getColor());
        assertEquals(brand, captured.getBrand());
        assertEquals(material, captured.getMaterial());
        assertEquals(category, captured.getCategory());
        assertEquals(subcategory, captured.getSubcategory());
        assertEquals(minPrice, captured.getMinPrice());
        assertEquals(maxPrice, captured.getMaxPrice());
        assertEquals(shopLocation, captured.getShopLocation());
        assertEquals(size, captured.getSize());

        verify(model).addAttribute("colors", List.of("Blue"));
        verify(model).addAttribute("brands", List.of("BrandX"));
        verify(model).addAttribute("materials", List.of("Silk"));
        verify(model).addAttribute("categories", List.of("Shirt"));
        verify(model).addAttribute("subcategories", List.of("Casual"));
        verify(model).addAttribute("sizes", List.of("M"));
        verify(model).addAttribute("shopLocations", List.of("Lisbon"));
        verify(model).addAttribute("items", filteredItems);
        verify(model).addAttribute("itemCount", 2);
    }

    @Test
    void testFilterItems_Post_BuildsRedirectUrlWithParams() {
        String res = userController.filterItems("men", "blue color", "Acme", "cotton", "Shirt", "Casual", 5.0, 20.0, "Lisbon Downtown", "M");
        assertEquals("redirect:/magiclook/items/men?color=blue+color&brand=Acme&material=cotton&category=Shirt&subcategory=Casual&size=M&minPrice=5.0&maxPrice=20.0&shopLocation=Lisbon+Downtown", res);
    }
    
    @Test
    void testLogout_ShouldInvalidateSession() {
        User user = new User();
        user.setUsername("testuser");
        session.setAttribute("loggedInUser", user);
        session.setAttribute("userId", UUID.randomUUID());

        String viewName = userController.logout(session);

        assertEquals("redirect:/magiclook/login?logout", viewName);

    }

    @Test
    void testDashboard_ShowsCorrectNumberOfRecentItems() {
        User user = new User();
        user.setUsername("testuser");
        session.setAttribute("loggedInUser", user);

        List<Item> recentItems = createTestItems("M", 4);
        when(itemService.getRecentItems(6)).thenReturn(recentItems);

        userController.showDashboard(session, model);

        verify(model).addAttribute("recentItems", recentItems);
        assertEquals(4, recentItems.size());
    }

    @Test
    void testDashboard_WithCartCount_ShouldIncludeCartCount() {
        User user = new User();
        user.setUsername("testuser");
        session.setAttribute("loggedInUser", user);
        session.setAttribute("cartCount", 5);

        when(itemService.getRecentItems(6)).thenReturn(new ArrayList<>());

        userController.showDashboard(session, model);

        verify(model).addAttribute("cartCount", 5);
    }

    @Test
    void testDashboard_WithoutCartCount_ShouldUseZero() {
        User user = new User();
        user.setUsername("testuser");
        session.setAttribute("loggedInUser", user);

        when(itemService.getRecentItems(6)).thenReturn(new ArrayList<>());

        userController.showDashboard(session, model);

        verify(model).addAttribute("cartCount", 0);
    }

    private List<Item> createTestItems(String gender, int count) {
        List<Item> items = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Item item = new Item();
            item.setItemId(i);
            item.setName("Item " + i + " " + gender);

            com.magiclook.data.ItemType itemType = new com.magiclook.data.ItemType();
            itemType.setGender(gender);
            item.setItemType(itemType);

            items.add(item);
        }
        return items;
    }

    @Test
    void testFilterItems_WithValidFilters() {
        User user = new User();
        user.setUsername("testuser");
        session.setAttribute("loggedInUser", user);

        ItemFilterDTO filter = new ItemFilterDTO();
        filter.setColor("Blue");

        List<Item> filteredItems = createTestItems(2);
        when(itemService.searchItemsWithFilters("M", "Blue", null, null, null, null, null, null))
                .thenReturn(filteredItems);

        String viewName = userController.filterItems("men", filter, session, model);

        assertEquals("items/men", viewName);
        verify(model).addAttribute("items", filteredItems);
        verify(model).addAttribute("hasFilters", true);
    }

    @Test
    void testFilterItems_WithoutLoggedInUser_ShouldRedirect() {
        ItemFilterDTO filter = new ItemFilterDTO();

        String viewName = userController.filterItems("women", filter, session, model);

        assertEquals("redirect:/magiclook/login", viewName);
    }

    @Test
    void testClearFilters_ShouldRedirect() {
        String viewName = userController.clearFilters("women", session);
        assertEquals("redirect:/magiclook/items/women", viewName);
    }

    // ==================== MARK NOTIFICATION AS READ TESTS ====================

    @Test
    void testMarkNotificationAsRead_WithValidNotification_ShouldReturnOk() {
        UUID notificationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        User user = new User();
        user.setUserId(userId);
        session.setAttribute("loggedInUser", user);
        
        com.magiclook.data.Notification notification = new com.magiclook.data.Notification();
        notification.setNotificationId(notificationId);
        notification.setUser(user);
        notification.setRead(false);
        
        when(notificationRepository.findById(notificationId))
                .thenReturn(java.util.Optional.of(notification));
        
        org.springframework.http.ResponseEntity<?> response = 
                userController.markNotificationAsRead(notificationId, session);
        
        assertEquals(org.springframework.http.HttpStatus.OK, response.getStatusCode());
        assertTrue(notification.isRead());
        verify(notificationRepository).save(notification);
    }

    @Test
    void testMarkNotificationAsRead_WithoutLoggedInUser_ShouldReturn401() {
        UUID notificationId = UUID.randomUUID();
        
        org.springframework.http.ResponseEntity<?> response = 
                userController.markNotificationAsRead(notificationId, session);
        
        assertEquals(org.springframework.http.HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(notificationRepository, never()).findById(any());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void testMarkNotificationAsRead_WithNonExistentNotification_ShouldReturn404() {
        UUID notificationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        User user = new User();
        user.setUserId(userId);
        session.setAttribute("loggedInUser", user);
        
        when(notificationRepository.findById(notificationId))
                .thenReturn(java.util.Optional.empty());
        
        org.springframework.http.ResponseEntity<?> response = 
                userController.markNotificationAsRead(notificationId, session);
        
        assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void testMarkNotificationAsRead_WithDifferentUser_ShouldReturn403() {
        UUID notificationId = UUID.randomUUID();
        UUID loggedInUserId = UUID.randomUUID();
        UUID notificationOwnerId = UUID.randomUUID();
        
        User loggedInUser = new User();
        loggedInUser.setUserId(loggedInUserId);
        session.setAttribute("loggedInUser", loggedInUser);
        
        User notificationOwner = new User();
        notificationOwner.setUserId(notificationOwnerId);
        
        com.magiclook.data.Notification notification = new com.magiclook.data.Notification();
        notification.setNotificationId(notificationId);
        notification.setUser(notificationOwner);
        notification.setRead(false);
        
        when(notificationRepository.findById(notificationId))
                .thenReturn(java.util.Optional.of(notification));
        
        org.springframework.http.ResponseEntity<?> response = 
                userController.markNotificationAsRead(notificationId, session);
        
        assertEquals(org.springframework.http.HttpStatus.FORBIDDEN, response.getStatusCode());
        assertFalse(notification.isRead());
        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void testMarkNotificationAsRead_AlreadyRead_ShouldStillReturnOk() {
        UUID notificationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        User user = new User();
        user.setUserId(userId);
        session.setAttribute("loggedInUser", user);
        
        com.magiclook.data.Notification notification = new com.magiclook.data.Notification();
        notification.setNotificationId(notificationId);
        notification.setUser(user);
        notification.setRead(true);
        
        when(notificationRepository.findById(notificationId))
                .thenReturn(java.util.Optional.of(notification));
        
        org.springframework.http.ResponseEntity<?> response = 
                userController.markNotificationAsRead(notificationId, session);
        
        assertEquals(org.springframework.http.HttpStatus.OK, response.getStatusCode());
        assertTrue(notification.isRead());
        verify(notificationRepository).save(notification);
    }

    private List<Item> createTestItems(int count) {
        List<Item> items = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Item item = new Item();
            item.setItemId(i);
            item.setName("Item " + i);
            items.add(item);
        }
        return items;
    }
}
