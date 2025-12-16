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

import java.util.List;

@Controller
@RequestMapping("/magiclook")
public class UserController {

    private final UserService userService;
    private final ItemService itemService;

    @Autowired
    public UserController(UserService userService, ItemService itemService) {
        this.userService = userService;
        this.itemService = itemService;
    }

    // ========== REGISTRO ==========
    
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new UserRegistrationDTO());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") UserRegistrationDTO userDTO,
                          BindingResult result,
                          Model model) {
        
        if (result.hasErrors()) {
            return "register";
        }
        
        try {
            userService.register(userDTO);
            model.addAttribute("success", "Registro realizado com sucesso! Faça login.");
            return "redirect:/magiclook/login?success"; // Redireciona para login
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    // ========== LOGIN ==========
    
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginRequest", new LoginDTO());
        
        if (model.containsAttribute("success")) {
            model.addAttribute("success", "Registro realizado com sucesso!");
        }
        
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                       @RequestParam String password,
                       HttpSession session,
                       Model model) {
        
        User user = userService.login(username, password);
        
        if (user != null) {
            session.setAttribute("loggedInUser", user);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("userName", user.getFirstName());
            
            return "redirect:/magiclook/dashboard";
        } else {
            model.addAttribute("error", "Usuário ou senha inválidos!");
            model.addAttribute("loginRequest", new LoginDTO(username, password));
            return "login";
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
        User user = (User) session.getAttribute("loggedInUser");
        
        if (user == null) {
            return "redirect:/magiclook/login";
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
        model.addAttribute("activePage", pageName);
        model.addAttribute("gender", pageName);
        
        return "items/" + pageName;
    }
    
    // ========== FILTRAR ITENS ==========
    
    @PostMapping("/items/{gender}/filter")
    public String filterItems(@PathVariable String gender,
                             @ModelAttribute ItemFilterDTO filter,
                             HttpSession session,
                             Model model) {
        
        User user = (User) session.getAttribute("loggedInUser");
        
        if (user == null) {
            return "redirect:/magiclook/login";
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
        model.addAttribute("activePage", gender);
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
        User user = (User) session.getAttribute("loggedInUser");
        
        if (user == null) {
            return "redirect:/magiclook/login";
        }
        
        // Buscar itens recentes
        List<Item> recentItems = itemService.getRecentItems(6);
        
        model.addAttribute("user", user);
        model.addAttribute("recentItems", recentItems);
        model.addAttribute("cartCount", session.getAttribute("cartCount") != null ? session.getAttribute("cartCount") : 0);
        model.addAttribute("activePage", "dashboard");
        return "dashboard";
    }

    // ========== LOGOUT ==========
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/magiclook/login?logout";
    }
}