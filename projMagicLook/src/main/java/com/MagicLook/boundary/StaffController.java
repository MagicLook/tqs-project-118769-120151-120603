package com.MagicLook.boundary;

import com.MagicLook.data.Staff;
import com.MagicLook.data.Item;
import com.MagicLook.service.StaffService;
import com.MagicLook.service.ItemService;
import com.MagicLook.dto.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/newitem")
    public ResponseEntity<String> addItem(@Valid @RequestBody ItemDTO itemDTO) {
        int result = staffService.addItem(itemDTO);

        if (result != 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Já existe um item com essas características");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("Item adicionado com sucesso.");
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