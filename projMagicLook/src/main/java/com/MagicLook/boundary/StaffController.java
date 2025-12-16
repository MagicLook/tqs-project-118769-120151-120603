package com.MagicLook.boundary;

import com.MagicLook.data.*;
import com.MagicLook.service.*;
import com.MagicLook.dto.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.math.BigDecimal;

@Controller
@RequestMapping("/magiclook/staff")
public class StaffController {

    @Autowired
    private StaffService staffService;
    
    @Autowired
    private ItemService itemService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    // ========== LOGIN STAFF ==========
    
    @GetMapping("/login")
    public String showStaffLoginForm(Model model) {
        model.addAttribute("staffLogin", new com.MagicLook.dto.StaffLoginDTO());
        return "staffLogin";
    }

    @PostMapping("/login")
    public String staffLogin(@RequestParam String usernameOrEmail,
                           @RequestParam String password,
                           HttpSession session,
                           Model model) {
        
        Staff staff = staffService.login(usernameOrEmail, password);
        
        if (staff != null) {
            session.setAttribute("loggedInStaff", staff);
            session.setAttribute("staffId", staff.getStaffId());
            session.setAttribute("staffName", staff.getName());
            session.setAttribute("staffEmail", staff.getEmail());
            session.setAttribute("staffUsername", staff.getUsername());
            session.setAttribute("shopId", staff.getShop().getShopId());
            session.setAttribute("shopName", staff.getShop().getName());
            
            return "redirect:/magiclook/staff/dashboard";
        } else {
            model.addAttribute("error", "Credenciais inválidas para staff!");
            return "staffLogin";
        }
    }

    // ========== DASHBOARD STAFF ==========
    
    @GetMapping("/dashboard")
    public String showStaffDashboard(HttpSession session, Model model) {
        Staff staff = (Staff) session.getAttribute("loggedInStaff");
        
        if (staff == null) {
            return "redirect:/magiclook/staff/login";
        }
        
        // Buscar itens da loja do staff
        List<Item> items = itemService.getItemsByShop(staff.getShop());
        
        model.addAttribute("staff", staff);
        model.addAttribute("shop", staff.getShop());
        model.addAttribute("items", items);
        model.addAttribute("itemCount", items.size());
        
        return "staffDashboard";
    }

    // ========== ADD ITEM ==========

    @PostMapping("/item")
    public String addItem(
            @RequestParam String name,
            @RequestParam String brand,
            @RequestParam String material,
            @RequestParam String color,
            @RequestParam String size,
            @RequestParam BigDecimal priceRent,
            @RequestParam BigDecimal priceSale,
            @RequestParam String gender,
            @RequestParam String category,
            @RequestParam String subcategory,
            @RequestParam Integer shop,
            @RequestParam(required = false) MultipartFile image,
            HttpSession session,
            Model model) {
        
        try {
            System.out.println("=== ADICIONAR ITEM ===");
            System.out.println("Name: " + name);
            System.out.println("Brand: " + brand);
            System.out.println("Material: " + material);
            System.out.println("Color: " + color);
            System.out.println("Size: " + size);
            System.out.println("Gender: " + gender);
            System.out.println("Category: " + category);
            System.out.println("Subcategory: " + subcategory);
            System.out.println("Shop ID: " + shop);

            Staff staff = (Staff) session.getAttribute("loggedInStaff");

            if (staff == null) {
                System.out.println("ERRO: Staff não está logado");
                return "redirect:/magiclook/staff/login";
            }
            
            System.out.println("Staff logado: " + staff.getName());

            // Converter em ItemDTO
            ItemDTO itemDTO = new ItemDTO(
                name,       
                material,   
                color,      
                brand,      
                size,       
                priceRent,  
                priceSale,  
                shop,
                gender,     
                category,
                subcategory 
            );
            
            System.out.println("Chamando staffService.addItem()...");
            int result = staffService.addItem(itemDTO);
            System.out.println("Resultado: " + result);
            
            // Resto de validações
            if (result == -1) {
                System.out.println("ERRO: Tamanho inválido");
                model.addAttribute("error", "Tamanho inválido!");
                return "staffDashboard";

            } else if (result == -2) {
                System.out.println("ERRO: Material inválido");
                model.addAttribute("error", "Material inválido!");
                return "staffDashboard";

            } else if (result == -3) {
                System.out.println("ERRO: Shop ou ItemType inválido");
                model.addAttribute("error", "Shop ou ItemType inválido!");
                return "staffDashboard";
            }

            // Guardar imagem se fornecida
            String imagePath = null;
            if (image != null && !image.isEmpty()) {
                System.out.println("Upload de imagem: " + image.getOriginalFilename());
                imagePath = staffService.saveImage(image, itemDTO.getItemId());
                System.out.println("Imagem guardada em: " + imagePath);
            }
            
            itemDTO.setImagePath(imagePath);
            
            System.out.println("Item adicionado com sucesso!");
            return "redirect:/magiclook/staff/dashboard";
            
        } catch (Exception e) {
            System.err.println("ERRO AO ADICIONAR ITEM: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Erro ao adicionar item: " + e.getMessage());
            return "staffDashboard";
        }
    }

    // ========== LOGOUT STAFF ==========
    
    @GetMapping("/logout")
    public String staffLogout(HttpSession session) {
        if (session != null) {
            session.removeAttribute("loggedInStaff");
            session.removeAttribute("staffId");
            session.removeAttribute("staffName");
            session.removeAttribute("staffEmail");
            session.removeAttribute("staffUsername");
            session.removeAttribute("shopId");
            session.removeAttribute("shopName");
        }
        return "redirect:/magiclook/staff/login?logout";
    }
}