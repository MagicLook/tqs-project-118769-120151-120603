package com.magiclook.authentication;

import com.MagicLook.boundary.StaffController;
import com.MagicLook.data.Staff;
import com.MagicLook.data.Shop;
import com.MagicLook.service.StaffService;
import com.MagicLook.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpSession;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffControllerTest {

    @Mock
    private StaffService staffService;

    @Mock
    private ItemService itemService;

    @Mock
    private Model model;

    @Mock
    private HttpSession session;

    @InjectMocks
    private StaffController staffController;

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
    void testStaffLogin_WithValidEmail_ShouldSetSessionAndRedirect() {
        String email = "staff@test.com";
        String password = "password123";
        
        when(staffService.login(email, password)).thenReturn(testStaff);

        String viewName = staffController.staffLogin(email, password, session, model);

        assertEquals("redirect:/magiclook/staff/dashboard", viewName);
        verify(session).setAttribute(eq("loggedInStaff"), eq(testStaff));
        verify(session).setAttribute(eq("staffId"), eq(testStaff.getStaffId()));
        verify(session).setAttribute(eq("staffName"), eq(testStaff.getName()));
        verify(session).setAttribute(eq("staffEmail"), eq(testStaff.getEmail()));
        verify(session).setAttribute(eq("staffUsername"), eq(testStaff.getUsername()));
        verify(session).setAttribute(eq("shopId"), eq(testShop.getShopId()));
        verify(session).setAttribute(eq("shopName"), eq(testShop.getName()));
        verify(staffService).login(email, password);
        verifyNoInteractions(model);
    }

    @Test
    void testStaffLogin_WithValidUsername_ShouldSetSessionAndRedirect() {
        String username = "staffuser";
        String password = "password123";
        
        when(staffService.login(username, password)).thenReturn(testStaff);

        String viewName = staffController.staffLogin(username, password, session, model);

        assertEquals("redirect:/magiclook/staff/dashboard", viewName);
        verify(session).setAttribute(eq("loggedInStaff"), eq(testStaff));
        verify(staffService).login(username, password);
    }

    @Test
    void testStaffLogin_WithInvalidCredentials_ShouldShowError() {
        String username = "wronguser";
        String password = "wrongpass";
        
        when(staffService.login(username, password)).thenReturn(null);

        String viewName = staffController.staffLogin(username, password, session, model);

        assertEquals("staffLogin", viewName);
        verify(model).addAttribute(eq("error"), eq("Credenciais inválidas para staff!"));
        verify(session, never()).setAttribute(anyString(), any());
        verify(staffService).login(username, password);
    }

    @Test
    void testStaffLogin_WithEmptyCredentials_ShouldShowError() {
        String username = "";
        String password = "";
        
        when(staffService.login(username, password)).thenReturn(null);

        String viewName = staffController.staffLogin(username, password, session, model);

        assertEquals("staffLogin", viewName);
        verify(model).addAttribute(eq("error"), eq("Credenciais inválidas para staff!"));
        verify(staffService).login(username, password);
    }

    @Test
    void testStaffLogout_ShouldRemoveSessionAttributesAndRedirect() {
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
    void testStaffLogout_WithNullSession_ShouldStillRedirect() {
        String viewName = staffController.staffLogout(null);

        assertEquals("redirect:/magiclook/staff/login?logout", viewName);
    }

    @Test
    void testShowStaffDashboard_WithLoggedInStaff_ShouldReturnDashboard() {
        when(session.getAttribute("loggedInStaff")).thenReturn(testStaff);
        when(itemService.getItemsByShop(testShop)).thenReturn(java.util.List.of());

        String viewName = staffController.showStaffDashboard(session, model);

        assertEquals("staffDashboard", viewName);
        verify(model).addAttribute(eq("staff"), eq(testStaff));
        verify(model).addAttribute(eq("shop"), eq(testShop));
        verify(model).addAttribute(eq("items"), any());
        verify(model).addAttribute(eq("itemCount"), any());
        verify(session).getAttribute("loggedInStaff");
        verify(itemService).getItemsByShop(testShop);
    }

    @Test
    void testShowStaffDashboard_WithoutLoggedInStaff_ShouldRedirectToLogin() {
        when(session.getAttribute("loggedInStaff")).thenReturn(null);

        String viewName = staffController.showStaffDashboard(session, model);

        assertEquals("redirect:/magiclook/staff/login", viewName);
        verify(model, never()).addAttribute(anyString(), any());
        verify(session).getAttribute("loggedInStaff");
        verify(itemService, never()).getItemsByShop(any());
    }
}