package com.MagicLook.service;

import com.MagicLook.data.Staff;
import com.MagicLook.data.Shop;
import com.MagicLook.repository.StaffRepository;
import com.MagicLook.repository.ShopRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class StaffService {
    
    @Autowired
    private StaffRepository staffRepository;
    
    @Autowired
    private ShopRepository shopRepository;
    
    @PostConstruct
    public void initializeStaff() {
        if (staffRepository.count() == 0) {
            // Criar lojas se não existirem
            List<Shop> shops = Arrays.asList(
                new Shop("Loja Centro", "Centro Comercial ABC"),
                new Shop("Loja Norte", "Shopping Norte"),
                new Shop("Loja Sul", "Avenida Sul, 123")
            );
            
            shops = shopRepository.saveAll(shops);
            
            List<Staff> staffList = Arrays.asList(
                new Staff("Ana Silva", "ana.silva@magiclook.com", "admin123", "anasi", shops.get(0)),
                new Staff("Carlos Santos", "carlos.santos@magiclook.com", "admin456", "carloss", shops.get(1)),
                new Staff("Maria Oliveira", "maria.oliveira@magiclook.com", "admin789", "mariao", shops.get(2))
            );
            
            staffRepository.saveAll(staffList);
            System.out.println("Staff inicializado com 3 administradores");
        }
    }
    
    public Staff login(String usernameOrEmail, String password) {
        // Tentar encontrar por email primeiro
        Optional<Staff> staffByEmail = staffRepository.findByEmail(usernameOrEmail);
        if (staffByEmail.isPresent()) {
            Staff staff = staffByEmail.get();
            if (staff.getPassword().equals(password)) {
                return staff;
            }
        }
        
        // Se não encontrou por email, tentar por username
        Optional<Staff> staffByUsername = staffRepository.findByUsername(usernameOrEmail);
        if (staffByUsername.isPresent()) {
            Staff staff = staffByUsername.get();
            if (staff.getPassword().equals(password)) {
                return staff;
            }
        }
        
        return null;
    }
    
    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }
}