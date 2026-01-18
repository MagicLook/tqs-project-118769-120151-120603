package com.magiclook.boundary;

import com.magiclook.dto.UserRegistrationDTO;
import com.magiclook.data.User;
import com.magiclook.service.UserService;
import com.magiclook.dto.LoginDTO;
import com.magiclook.service.ItemService;
import com.magiclook.repository.NotificationRepository;
import com.magiclook.data.Item;
import com.magiclook.dto.ItemFilterDTO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import io.micrometer.core.annotation.Timed;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.AbstractMap; 

@Controller
@RequestMapping("/magiclook")
public class UserController {
    
    private final UserService userService;
    private final ItemService itemService;
    private final NotificationRepository notificationRepository;

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
    private static final String ITEMS = "items";
    private static final String ITEM_COUNT = "itemCount";

    @Autowired
    public UserController(UserService userService, ItemService itemService,
            NotificationRepository notificationRepository) {
        this.userService = userService;
        this.itemService = itemService;
        this.notificationRepository = notificationRepository;
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
    @Timed(value = "request.getMenItems")
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

    // Convenience overload for unit tests (direct call)
    public String showMenItems(HttpSession session, Model model) {
        User user = (User) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (user == null) {
            return REDIRECT_LOGIN;
        }

        List<Item> items = itemService.getItemsByGender("M");
        model.addAttribute("user", user);
        model.addAttribute(ITEMS, items);
        model.addAttribute(ITEM_COUNT, items.size());
        model.addAttribute(ATTR_ACTIVE_PAGE, "men");
        return "items/men";
    }

    @GetMapping("/items/women")
    @Timed(value = "request.getWomenItems")
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

    // Convenience overload for unit tests (direct call)
    public String showWomenItems(HttpSession session, Model model) {
        User user = (User) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (user == null) {
            return REDIRECT_LOGIN;
        }

        List<Item> items = itemService.getItemsByGender("F");
        model.addAttribute("user", user);
        model.addAttribute(ITEMS, items);
        model.addAttribute(ITEM_COUNT, items.size());
        model.addAttribute(ATTR_ACTIVE_PAGE, "women");
        return "items/women";
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

        long unreadNotifications = notificationRepository.countByUserAndReadFalse(user);
        model.addAttribute("unreadNotifications", unreadNotifications);

        model.addAttribute("subcategories", itemService.getAllDistinctSubcategoriesByGender(genderCode));
        model.addAttribute("sizes", itemService.getAllDistinctSizesByGender(genderCode));
        model.addAttribute("shopLocations", itemService.getAllDistinctShopLocations());

        model.addAttribute("user", user);
        model.addAttribute(ITEMS, items);
        model.addAttribute(ITEM_COUNT, items.size());
        model.addAttribute(ATTR_ACTIVE_PAGE, pageName);
        model.addAttribute("gender", pageName);
        model.addAttribute("hasFilters", filter.hasFilters());

        return "items/" + pageName;
    }

    // ========== FILTRAR ITENS ==========

    @PostMapping("/items/{gender}/filter")
    @Timed(value = "request.filterItems")
    public String filterItems(
            @PathVariable String gender,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subcategory,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String shopLocation,
            @RequestParam(required = false) String size) {

        StringBuilder redirectUrl = new StringBuilder("redirect:/magiclook/items/").append(gender);
        boolean firstParam = true;

        // Create a list of parameter entries
        List<Map.Entry<String, Object>> params = new ArrayList<>();
        params.add(new AbstractMap.SimpleEntry<>("color", color));
        params.add(new AbstractMap.SimpleEntry<>("brand", brand));
        params.add(new AbstractMap.SimpleEntry<>("material", material));
        params.add(new AbstractMap.SimpleEntry<>("category", category));
        params.add(new AbstractMap.SimpleEntry<>("subcategory", subcategory));
        params.add(new AbstractMap.SimpleEntry<>("size", size));
        params.add(new AbstractMap.SimpleEntry<>("minPrice", minPrice));
        params.add(new AbstractMap.SimpleEntry<>("maxPrice", maxPrice));
        params.add(new AbstractMap.SimpleEntry<>("shopLocation", shopLocation));

        for (Map.Entry<String, Object> param : params) {
            if (shouldIncludeParameter(param.getValue())) {
                redirectUrl.append(firstParam ? "?" : "&")
                        .append(param.getKey())
                        .append("=")
                        .append(encodeParameter(param.getValue()));
                firstParam = false;
            }
        }

        return redirectUrl.toString();
    }

    private boolean shouldIncludeParameter(Object value) {
        return value != null && (!(value instanceof String string) || !string.isEmpty());
    }

    private String encodeParameter(Object value) {
        return value instanceof String string ? 
               URLEncoder.encode(string, StandardCharsets.UTF_8) : 
               value.toString();
    }

    // Convenience overload for unit tests that takes an ItemFilterDTO
    public String filterItems(String gender, ItemFilterDTO filter, HttpSession session, Model model) {
        User user = (User) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (user == null) {
            return REDIRECT_LOGIN;
        }

        String genderCode = "men".equals(gender) ? "M" : "F";

        List<Item> items = itemService.searchItemsWithFilters(
                genderCode,
                filter.getColor(),
                filter.getBrand(),
                filter.getMaterial(),
                filter.getCategory(),
                filter.getShopLocation(),
                filter.getMinPrice(),
                filter.getMaxPrice());

        model.addAttribute("filter", filter);
        model.addAttribute(ITEMS, items);
        model.addAttribute("hasFilters", filter.hasFilters());
        model.addAttribute(ITEM_COUNT, items.size());
        model.addAttribute("user", user);
        model.addAttribute(ATTR_ACTIVE_PAGE, gender);

        return "items/" + gender;
    }

    // ========== LIMPAR FILTROS ==========

    @GetMapping("/items/{gender}/clear")
    @Timed(value = "request.clearItemFilters")
    public String clearFilters(@PathVariable String gender, HttpSession session) {
        return "redirect:/magiclook/items/" + gender;
    }

    // ============== DASHBOARD ===============

    @GetMapping("/dashboard")
    @Timed(value = "request.getDashboard")
    public String showDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute(ATTR_LOGGED_IN_USER);

        if (user == null) {
            return REDIRECT_LOGIN;
        }

        // Buscar itens recentes
        List<Item> recentItems = itemService.getRecentItems(6);

        // Notifications
        long unreadNotifications = notificationRepository.countByUserAndReadFalse(user);
        model.addAttribute("unreadNotifications", unreadNotifications);
        model.addAttribute("notifications", notificationRepository.findByUserAndReadFalseOrderByDateDesc(user));

        model.addAttribute("user", user);
        model.addAttribute("recentItems", recentItems);
        model.addAttribute(ATTR_CART_COUNT,
                session.getAttribute(ATTR_CART_COUNT) != null ? session.getAttribute(ATTR_CART_COUNT) : 0);
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

    // ========== NOTIFICATIONS ==========

    @PostMapping("/notification/read/{id}")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> markNotificationAsRead(
            @PathVariable java.util.UUID id,
            HttpSession session) {

        User user = (User) session.getAttribute(ATTR_LOGGED_IN_USER);
        if (user == null) {
            return org.springframework.http.ResponseEntity.status(401).build();
        }

        return notificationRepository.findById(id)
                .map(notification -> {
                    if (!notification.getUser().getUserId().equals(user.getUserId())) {
                        return org.springframework.http.ResponseEntity.status(403).build();
                    }
                    notification.setRead(true);
                    notificationRepository.save(notification);
                    return org.springframework.http.ResponseEntity.ok().build();
                })
                .orElse(org.springframework.http.ResponseEntity.notFound().build());
    }
}