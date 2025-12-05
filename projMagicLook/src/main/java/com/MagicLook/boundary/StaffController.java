package com.MagicLook.boundary;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.MagicLook.service.StaffService;
import com.MagicLook.dto.*;

@RestController
@RequestMapping("magiclook")
public class StaffController {
    private StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @PostMapping("/item")
    public ResponseEntity<String> addItem(@Valid @RequestBody ItemDTO itemDTO) {
        int result = staffService.addItem(itemDTO);

        if (result != 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Já existe um item com essas características");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("Item adicionado com sucesso.");
    }
}
