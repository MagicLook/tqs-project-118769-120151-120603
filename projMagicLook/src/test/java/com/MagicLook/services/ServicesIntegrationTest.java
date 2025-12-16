package com.magiclook.services;

import com.MagicLook.service.ItemService;
import com.MagicLook.service.StaffService;
import com.MagicLook.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ServicesIntegrationTest {

    @Autowired(required = false)
    private UserService userService;

    @Autowired(required = false)
    private StaffService staffService;

    @Autowired(required = false)
    private ItemService itemService;

    @Test
    void contextLoads() {
        // Verifica se os serviços são carregados
        assertThat(userService).isNotNull();
        assertThat(staffService).isNotNull();
        assertThat(itemService).isNotNull();
    }

    @Test
    void testServicesAreInstancesOfCorrectType() {
        assertThat(userService).isInstanceOf(UserService.class);
        assertThat(staffService).isInstanceOf(StaffService.class);
        assertThat(itemService).isInstanceOf(ItemService.class);
    }
}