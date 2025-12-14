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
            @RequestParam Integer shop,
            @RequestParam(required = false) MultipartFile image,
            HttpSession session,
            Model model) {
        
        try {

            Staff staff = (Staff) session.getAttribute("loggedInStaff");

            if (staff == null) {
                return "redirect:/magiclook/staff/login";
            }

            // Converter em ItemDDTO
            ItemDTO itemDTO = new ItemDTO(name, material, color, brand, size,
                                          priceRent, priceSale, shop, gender, category);
            
            int result = staffService.addItem(itemDTO);
        
            // Resto de validações
            if (result == -1) {
                model.addAttribute("error", "Tamanho inválido!");
                return "staffDashboard";
            } else if (result == -2) {
                model.addAttribute("error", "Material inválido!");
                return "staffDashboard";
            }

            // Guardar imagem se fornecida
            String imagePath = null;
            if (image != null && !image.isEmpty()) {
                staffService.saveImage(image, 1L);
            }
            
            // Criar novo item
            Item item = new Item();
            item.setName(name);
            item.setBrand(brand);
            item.setMaterial(material);
            item.setColor(color);
            item.setSize(size);
            item.setPriceRent(priceRent);
            item.setPriceSale(priceSale);
            item.setImagePath(imagePath);
            item.setShop(staff.getShop());
            
            // TODO: Buscar ou criar ItemType baseado em gender e category
            // Por enquanto, isso seria feito no service
            
            itemService.save(item);
            
            return "redirect:/magiclook/staff/dashboard";
            
        } catch (Exception e) {
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