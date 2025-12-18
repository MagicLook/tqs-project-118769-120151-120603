package com.magiclook.boundary;

import com.magiclook.dto.UserRegistrationDTO;
import com.magiclook.data.User;
import com.magiclook.service.UserService;
import com.magiclook.dto.LoginDTO;
import com.magiclook.service.ItemService;
import com.magiclook.data.Item;
import com.magiclook.dto.ItemFilterDTO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
            model.addAttribute(ATTR_ERROR, "Username ou palavra-passe inválidos!");
            model.addAttribute("loginRequest", new LoginDTO(username, password));
            return VIEW_LOGIN;
        }
    }

    // ========== ITEMS (Homens e Mulheres) ==========

    @GetMapping("/items/men")
    public String showMenItems(
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subcategory,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String shopLocation,
            @RequestParam(required = false) String size,
            HttpSession session,
            Model model) {
        
        return showGenderItems(session, model, "M", "men", 
                color, brand, material, category, subcategory, 
                minPrice, maxPrice, shopLocation, size);
    }

    @GetMapping("/items/women")
    public String showWomenItems(
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subcategory,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String shopLocation,
            @RequestParam(required = false) String size,
            HttpSession session,
            Model model) {
        
        return showGenderItems(session, model, "F", "women", 
                color, brand, material, category, subcategory, 
                minPrice, maxPrice, shopLocation, size);
    }
    
    private String showGenderItems(HttpSession session, Model model, 
                                   String genderCode, String pageName,
                                   String color, String brand, String material, 
                                   String category, String subcategory,
                                   Double minPrice, Double maxPrice,
                                   String shopLocation, String size) {
        
        User user = (User) session.getAttribute(ATTR_LOGGED_IN_USER);
        
        if (user == null) {
            return REDIRECT_LOGIN;
        }
        
        // Criar filtro com os parâmetros
        ItemFilterDTO filter = new ItemFilterDTO(color, brand, material, category, 
                                               subcategory, minPrice, maxPrice, 
                                               shopLocation, size);
        
        // Buscar itens com filtros
        List<Item> items = itemService.findByGenderAndFilters(genderCode, filter);
        
        // Obter opções para os filtros
        model.addAttribute("filter", filter);
        model.addAttribute("colors", itemService.getAllDistinctColors());
        model.addAttribute("brands", itemService.getAllDistinctBrands());
        model.addAttribute("materials", itemService.getAllDistinctMaterials());
        model.addAttribute("categories", itemService.getAllDistinctCategories());
        model.addAttribute("subcategories", itemService.getAllDistinctSubcategoriesByGender(genderCode));
        model.addAttribute("sizes", itemService.getAllDistinctSizesByGender(genderCode));
        model.addAttribute("shopLocations", itemService.getAllDistinctShopLocations());
        
        model.addAttribute("user", user);
        model.addAttribute("items", items);
        model.addAttribute("itemCount", items.size());
        model.addAttribute(ATTR_ACTIVE_PAGE, pageName);
        model.addAttribute("gender", pageName);
        model.addAttribute("hasFilters", filter.hasFilters());
        
        return "items/" + pageName;
    }
    
    // ========== FILTRAR ITENS ==========
    
    @PostMapping("/items/{gender}/filter")
    public String filterItems(@PathVariable String gender,
                            @RequestParam(required = false) String color,
                            @RequestParam(required = false) String brand,
                            @RequestParam(required = false) String material,
                            @RequestParam(required = false) String category,
                            @RequestParam(required = false) String subcategory,
                            @RequestParam(required = false) Double minPrice,
                            @RequestParam(required = false) Double maxPrice,
                            @RequestParam(required = false) String shopLocation,
                            @RequestParam(required = false) String size) {
        
        // Construir URL com parâmetros
        StringBuilder redirectUrl = new StringBuilder("redirect:/magiclook/items/").append(gender);
        boolean firstParam = true;
        
        if (color != null && !color.isEmpty()) {
            redirectUrl.append(firstParam ? "?" : "&").append("color=").append(URLEncoder.encode(color, StandardCharsets.UTF_8));
            firstParam = false;
        }
        if (brand != null && !brand.isEmpty()) {
            redirectUrl.append(firstParam ? "?" : "&").append("brand=").append(URLEncoder.encode(brand, StandardCharsets.UTF_8));
            firstParam = false;
        }
        if (material != null && !material.isEmpty()) {
            redirectUrl.append(firstParam ? "?" : "&").append("material=").append(URLEncoder.encode(material, StandardCharsets.UTF_8));
            firstParam = false;
        }
        if (category != null && !category.isEmpty()) {
            redirectUrl.append(firstParam ? "?" : "&").append("category=").append(URLEncoder.encode(category, StandardCharsets.UTF_8));
            firstParam = false;
        }
        if (subcategory != null && !subcategory.isEmpty()) {
            redirectUrl.append(firstParam ? "?" : "&").append("subcategory=").append(URLEncoder.encode(subcategory, StandardCharsets.UTF_8));
            firstParam = false;
        }
        if (size != null && !size.isEmpty()) {
            redirectUrl.append(firstParam ? "?" : "&").append("size=").append(URLEncoder.encode(size, StandardCharsets.UTF_8));
            firstParam = false;
        }
        if (minPrice != null) {
            redirectUrl.append(firstParam ? "?" : "&").append("minPrice=").append(minPrice);
            firstParam = false;
        }
        if (maxPrice != null) {
            redirectUrl.append(firstParam ? "?" : "&").append("maxPrice=").append(maxPrice);
            firstParam = false;
        }
        if (shopLocation != null && !shopLocation.isEmpty()) {
            redirectUrl.append(firstParam ? "?" : "&").append("shopLocation=").append(URLEncoder.encode(shopLocation, StandardCharsets.UTF_8));
            firstParam = false;
        }
        
        return redirectUrl.toString();
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
        model.addAttribute(ATTR_ACTIVE_PAGE, VIEW_DASHBOARD);
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