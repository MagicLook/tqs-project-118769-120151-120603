package com.magiclook.boundary;

import com.magiclook.data.*;
import com.magiclook.service.*;
import com.magiclook.dto.*;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.math.BigDecimal;

@Controller
@RequestMapping("/magiclook/staff")
public class StaffController {

    // Constants
    private static final String STAFF_DASHBOARD_VIEW = "staffDashboard";
    private static final String STAFF_LOGIN_VIEW = "staffLogin";
    private static final String STAFF_ITEM_VIEW = "staffItem";
    private static final String STAFF_ITEM_DETAILS_VIEW = "staffItemDetails";
    private static final String ERROR = "error";

    @Autowired
    private StaffService staffService;

    @Autowired
    private ItemService itemService;

    @Autowired
    public StaffController(StaffService staffService, ItemService itemService) {
        this.staffService = staffService;
        this.itemService = itemService;
    }

    // ========== LOGIN STAFF ==========

    @GetMapping("/login")
    public String showStaffLoginForm(Model model) {
        model.addAttribute(STAFF_LOGIN_VIEW, new com.magiclook.dto.StaffLoginDTO());
        return STAFF_LOGIN_VIEW;
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
            return STAFF_LOGIN_VIEW;
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

        return STAFF_DASHBOARD_VIEW;
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
            Staff staff = (Staff) session.getAttribute("loggedInStaff");

            if (staff == null) {
                return "redirect:/magiclook/staff/login";
            }

            // Converter em ItemDTO
            ItemDTO itemDTO = new ItemDTO(
                    name,
                    material,
                    color,
                    brand,
                    priceRent,
                    priceSale,
                    shop,
                    gender,
                    category,
                    subcategory);

            int result = staffService.addItem(itemDTO, size);

            // Resto de validações
            if (result == -1) {
                model.addAttribute(ERROR, "Tamanho inválido!");
                return STAFF_DASHBOARD_VIEW;

            } else if (result == -2) {
                model.addAttribute(ERROR, "Material inválido!");
                return STAFF_DASHBOARD_VIEW;

            } else if (result == -3) {
                model.addAttribute(ERROR, "Shop ou ItemType inválido!");
                return STAFF_DASHBOARD_VIEW;
            }

            // Guardar imagem se fornecida e persistir no item
            String imagePath = null;
            if (image != null && !image.isEmpty()) {
                imagePath = staffService.saveImage(image, itemDTO.getItemId());
                staffService.updateItemImage(itemDTO.getItemId(), imagePath);
            }

            itemDTO.setImagePath(imagePath);

            return "redirect:/magiclook/staff/dashboard";

        } catch (Exception e) {
            model.addAttribute(ERROR, "Erro ao adicionar item: " + e.getMessage());
            return STAFF_DASHBOARD_VIEW;
        }
    }

    // ========== VIEW ITEMS ============

    @GetMapping("/item")
    public String getItems(
            HttpSession session,
            Model model,
            @RequestParam(name = "state", required = false) String state,
            @RequestParam(name = "q", required = false) String q) {

        Staff staff = (Staff) session.getAttribute("loggedInStaff");
        if (staff == null) {
            return "redirect:/magiclook/staff/login";
        }

        // Base list: items from this staff's shop
        List<Item> items = itemService.getItemsByShop(staff.getShop());

        // Optional name search
        if (q != null && !q.isBlank()) {
            String needle = q.trim().toLowerCase();
            items = items.stream()
                    .filter(i -> i.getName() != null && i.getName().toLowerCase().contains(needle))
                    .toList();
        }

        // Optional state filter: keep items that have at least one ItemSingle in that
        // state
        if (state != null && !state.isBlank()) {
            state = state.trim().toUpperCase();
            items = itemService.getAllItemsByState(state);
        }

        // Build map of itemId -> list of sizes
        java.util.Map<Integer, List<String>> itemSizes = new java.util.HashMap<>();
        for (Item item : items) {
            List<String> sizes = itemService.getItems(item.getItemId()).stream()
                    .map(ItemSingle::getSize)
                    .distinct()
                    .sorted()
                    .toList();
            itemSizes.put(item.getItemId(), sizes);
        }

        model.addAttribute("staff", staff);
        model.addAttribute("shop", staff.getShop());
        model.addAttribute("items", items);
        model.addAttribute("itemCount", items.size());
        model.addAttribute("itemSizes", itemSizes);

        // Bind filter values back to the view
        model.addAttribute("selectedState", state);
        model.addAttribute("q", q);

        return STAFF_ITEM_VIEW;
    }

    // ========== VIEW ITEM DETAILS =====

    @GetMapping("/item/{itemId}")
    public String getItemDetails(
            @PathVariable Integer itemId,
            HttpSession session,
            Model model) {

        Staff staff = (Staff) session.getAttribute("loggedInStaff");
        if (staff == null) {
            return "redirect:/magiclook/staff/login";
        }

        Item item = itemService.getItemById(itemId);
        List<ItemSingle> itemsList = itemService.getItems(itemId);

        model.addAttribute("staff", staff);
        model.addAttribute("shop", staff.getShop());
        model.addAttribute("item", item);
        model.addAttribute("itemSingles", itemsList);

        return STAFF_ITEM_DETAILS_VIEW;
    }

    // ========== UPDATE ITEM ===========

    @PostMapping("/item/{itemId}")
    public String updateItem(
            @PathVariable Integer itemId,
            @RequestParam String name,
            @RequestParam String brand,
            @RequestParam String material,
            @RequestParam String color,
            @RequestParam BigDecimal priceRent,
            @RequestParam BigDecimal priceSale,
            @RequestParam String gender,
            @RequestParam String category,
            @RequestParam String subcategory,
            @RequestParam Integer shop,
            @RequestParam(required = false) MultipartFile image,
            HttpSession session,
            Model model) {

        Staff staff = (Staff) session.getAttribute("loggedInStaff");
        if (staff == null) {
            return "redirect:/magiclook/staff/login";
        }

        try {
            ItemDTO itemDTO = new ItemDTO(
                    name,
                    material,
                    color,
                    brand,
                    priceRent,
                    priceSale,
                    shop,
                    gender,
                    category,
                    subcategory);
            itemDTO.setItemId(itemId);

            staffService.updateItem(itemDTO);

            if (image != null && !image.isEmpty()) {
                String imagePath = staffService.saveImage(image, itemId);
                staffService.updateItemImage(itemId, imagePath);
            }

            return "redirect:/magiclook/staff/item";

        } catch (Exception e) {
            Item item = itemService.getItemById(itemId);
            List<ItemSingle> itemsList = itemService.getItems(itemId);

            model.addAttribute("staff", staff);
            model.addAttribute("shop", staff.getShop());
            model.addAttribute("item", item);
            model.addAttribute("itemSingles", itemsList);
            model.addAttribute(ERROR, "Erro ao atualizar item: " + e.getMessage());

            return STAFF_ITEM_DETAILS_VIEW;
        }
    }

    @DeleteMapping("/item/{itemId}/size/{size}")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> deleteItemSize(
            @PathVariable Integer itemId,
            @PathVariable String size,
            HttpSession session) {

        Staff staff = (Staff) session.getAttribute("loggedInStaff");
        if (staff == null) {
            return org.springframework.http.ResponseEntity.status(401).build();
        }

        try {
            staffService.deleteItemSize(itemId, size);
            return org.springframework.http.ResponseEntity.ok().build();
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/itemsingle/update/{id}")
    public String updateItemSingle(
            @PathVariable java.util.UUID id,
            @RequestParam String size,
            @RequestParam String state,
            @RequestParam Integer itemId,
            HttpSession session) {

        Staff staff = (Staff) session.getAttribute("loggedInStaff");
        if (staff == null) {
            return "redirect:/magiclook/staff/login";
        }

        staffService.updateItemSingle(id, size, state);

        return "redirect:/magiclook/staff/item/" + itemId;
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