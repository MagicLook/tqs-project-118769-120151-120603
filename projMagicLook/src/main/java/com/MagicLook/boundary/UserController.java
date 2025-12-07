package com.MagicLook.boundary;

import com.MagicLook.dto.UserRegistrationDTO;
import com.MagicLook.data.User;
import com.MagicLook.service.UserService;
import com.MagicLook.dto.LoginDTO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/magiclook")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
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

    // ========== DASHBOARD (após login) ==========
    
    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        
        if (user == null) {
            return "redirect:/magiclook/login";
        }
        
        model.addAttribute("user", user);
        return "dashboard";
    }

    // ========== LOGOUT ==========
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/magiclook/login?logout";
    }
}