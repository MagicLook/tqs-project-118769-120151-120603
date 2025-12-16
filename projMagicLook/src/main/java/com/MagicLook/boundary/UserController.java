package com.MagicLook.boundary;

import com.MagicLook.dto.UserRegistrationDTO;
import com.MagicLook.data.User;
import com.MagicLook.service.UserService;
import com.MagicLook.dto.LoginDTO;
import com.MagicLook.service.ItemService;
import com.MagicLook.data.Item;
import com.MagicLook.dto.ItemFilterDTO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/magiclook")
public class UserController {

    private final UserService userService;
    private final ItemService itemService;
    
    // Constantes para evitar strings duplicadas
    private static final String VIEW_REGISTER = "register";
    private static final String VIEW_LOGIN = "login";
    private static final String ATTR_SUCCESS = "success";
    private static final String ATTR_ERROR = "error";
    private static final String ATTR_LOGGED_IN_USER = "loggedInUser";
    private static final String REDIRECT_LOGIN = "redirect:/magiclook/login";
    private static final String ATTR_ACTIVE_PAGE = "activePage";
    private static final String ATTR_CART_COUNT = "cartCount";
    private static final String VIEW_DASHBOARD = "dashboard";

    @Autowired
    public UserController(UserService userService, ItemService itemService) {
        this.userService = userService;
        this.itemService = itemService;
    }

    // ========== REGISTRO ==========
    
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new UserRegistrationDTO());
        return VIEW_REGISTER;
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") UserRegistrationDTO userDTO,
                          BindingResult result,
                          Model model) {
        
        if (result.hasErrors()) {
            return VIEW_REGISTER;
        }
        
        try {
            userService.register(userDTO);
            model.addAttribute(ATTR_SUCCESS, "Registro realizado com sucesso! Faça login.");
            return REDIRECT_LOGIN + "?success"; // Redireciona para login
        } catch (RuntimeException e) {
            model.addAttribute(ATTR_ERROR, e.getMessage());
            return VIEW_REGISTER;
        }
    }

    // ========== LOGIN ==========
    
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginRequest", new LoginDTO());
        
        if (model.containsAttribute(ATTR_SUCCESS)) {
            model.addAttribute(ATTR_SUCCESS, "Registro realizado com sucesso!");
        }
        
        return VIEW_LOGIN;
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                       @RequestParam String password,
                       HttpSession session,
                       Model model) {
        
        User user = userService.login(username, password);
        
        if (user != null) {
            session.setAttribute(ATTR_LOGGED_IN_USER, user);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("userName", user.getFirstName());
            
            return "redirect:/magiclook/dashboard";
        } else {
            model.addAttribute(ATTR_ERROR, "Utilizador ou palavra-passe inválidos!");
            model.addAttribute("loginRequest", new LoginDTO(username, password));
            return VIEW_LOGIN;
        }
    }

    // ========== ITEMS (Homens e Mulheres) ==========

    @GetMapping("/items/men")
    public String showMenItems(HttpSession session, Model model) {
        return showGenderItems(session, model, "M", "men");
    }

    @GetMapping("/items/women")
    public String showWomenItems(HttpSession session, Model model) {
        return showGenderItems(session, model, "F", "women");
    }
    
    private String showGenderItems(HttpSession session, Model model, String gender, String pageName) {
        User user = (User) session.getAttribute(ATTR_LOGGED_IN_USER);
        
        if (user == null) {
            return REDIRECT_LOGIN;
        }
        
        // Buscar itens para o gênero específico (sem filtros)
        List<Item> items = itemService.getItemsByGender(gender);
        
        // Adicionar dados para os filtros
        model.addAttribute("filter", new ItemFilterDTO());
        model.addAttribute("colors", itemService.getAllDistinctColors());
        model.addAttribute("brands", itemService.getAllDistinctBrands());
        model.addAttribute("materials", itemService.getAllDistinctMaterials());
        model.addAttribute("categories", itemService.getAllDistinctCategories());
        
        model.addAttribute("user", user);
        model.addAttribute("items", items);
        model.addAttribute("itemCount", items.size());
        model.addAttribute(ATTR_ACTIVE_PAGE, pageName);
        model.addAttribute("gender", pageName);
        
        return "items/" + pageName;
    }
    
    // ========== FILTRAR ITENS ==========
    
    @PostMapping("/items/{gender}/filter")
    public String filterItems(@PathVariable String gender,
                             @ModelAttribute ItemFilterDTO filter,
                             HttpSession session,
                             Model model) {
        
        User user = (User) session.getAttribute(ATTR_LOGGED_IN_USER);
        
        if (user == null) {
            return REDIRECT_LOGIN;
        }
        
        String genderCode = "women".equals(gender) ? "F" : "M";
        
        // Buscar itens com filtros
        List<Item> filteredItems = itemService.searchItemsWithFilters(
            genderCode,
            filter.getColor(),
            filter.getBrand(),
            filter.getMaterial(),
            filter.getCategory(),
            filter.getMinPrice(),
            filter.getMaxPrice()
        );
        
        // Adicionar dados para os filtros
        model.addAttribute("filter", filter);
        model.addAttribute("colors", itemService.getAllDistinctColors());
        model.addAttribute("brands", itemService.getAllDistinctBrands());
        model.addAttribute("materials", itemService.getAllDistinctMaterials());
        model.addAttribute("categories", itemService.getAllDistinctCategories());
        
        model.addAttribute("user", user);
        model.addAttribute("items", filteredItems);
        model.addAttribute("itemCount", filteredItems.size());
        model.addAttribute(ATTR_ACTIVE_PAGE, gender);
        model.addAttribute("gender", gender);
        model.addAttribute("hasFilters", filter.hasFilters());
        
        return "items/" + gender;
    }
    
    // ========== LIMPAR FILTROS ==========
    
    @GetMapping("/items/{gender}/clear")
    public String clearFilters(@PathVariable String gender, HttpSession session) {
        return "redirect:/magiclook/items/" + gender;
    }

    // ============== DASHBOARD ===============

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute(ATTR_LOGGED_IN_USER);
        
        if (user == null) {
            return REDIRECT_LOGIN;
        }
        
        // Buscar itens recentes
        List<Item> recentItems = itemService.getRecentItems(6);
        
        model.addAttribute("user", user);
        model.addAttribute("recentItems", recentItems);
        model.addAttribute(ATTR_CART_COUNT, session.getAttribute(ATTR_CART_COUNT) != null ? session.getAttribute(ATTR_CART_COUNT) : 0);
        model.addAttribute(ATTR_ACTIVE_PAGE, "dashboard");
        return VIEW_DASHBOARD;
    }

    // ========== LOGOUT ==========
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return REDIRECT_LOGIN + "?logout";
    }
}