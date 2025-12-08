package com.MagicLook.mainpage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.MagicLook.boundary.UserController;
import com.MagicLook.data.User;
import com.MagicLook.data.Item;
import com.MagicLook.service.UserService;
import com.MagicLook.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.springframework.mock.web.MockHttpSession;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserControllerTest {
    
    private UserController userController;
    
    @Mock
    private UserService userService;
    
    @Mock
    private ItemService itemService;
    
    @Mock
    private Model model;
    
    private MockHttpSession session;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userController = new UserController(userService, itemService);
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
            item.setItemId(UUID.randomUUID());
            item.setName("Item " + i + " " + gender);
            
            com.MagicLook.data.ItemType itemType = new com.MagicLook.data.ItemType();
            itemType.setGender(gender);
            item.setItemType(itemType);
            
            items.add(item);
        }
        return items;
    }
}