package com.magiclook.tests;

import com.magiclook.data.*;
import com.magiclook.repository.*;
import com.magiclook.service.*;
import com.magiclook.dto.ItemDTO;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;

import org.assertj.core.api.Assertions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StaffControllerIT {

        @LocalServerPort
        private int port;

        @Autowired
        private TestRestTemplate restTemplate;

        @Autowired
        private StaffService staffService;
        
        @Autowired
        private ItemRepository itemRepository; 
        
        @Autowired
        private ItemSingleRepository itemSingleRepository; // Add this to verify DB persistence
        
        private String jSessionId;  // Store session cookie
        
        private String seededUsername; 
        private String seededPassword;
        private Shop seededShop;
        private Integer seededShopId;

        @BeforeEach
        void setUp() {
                seededUsername = "admin";
                seededPassword = "admin123";

                Staff staff = staffService.login(seededUsername, seededPassword);
                Assertions.assertThat(staff).isNotNull();
                
                seededShop = staff.getShop();
                seededShopId = seededShop.getShopId();
        }

        @Test
        @Requirement("SCRUM-8")
        @DisplayName("POST /magiclook/staff/item → success creates item and redirects")
        void addItem_success_createsItemAndRedirectsToDashboard() {
                String sessionCookie = loginAsStaff(seededUsername, seededPassword);
                
                // Check if exists
                Optional<Item> foundItem = itemRepository.findByAllCharacteristics("Vestido Azul", "Seda", "Azul", "Zara", "F", "Vestido", "Curto", seededShopId);
                
                // 2. Get initial itemSingle instances for that item
                Integer initialCount = 0;
                List<ItemSingle> itemSingles = List.of();
                if (foundItem.isPresent()) {

                        Item item = foundItem.get();
                        
                        itemSingles = itemSingleRepository.findByItem_ItemId(item.getItemId());
                        initialCount = itemSingles.size();

                }
                
                // Make POST request
                ResponseEntity<String> response = restTemplate.exchange(
                        url("/magiclook/staff/item"),
                        HttpMethod.POST,
                        multipartBody(false, null, sessionCookie),
                        String.class);

                Assertions.assertThat(response.getStatusCode())
                        .isEqualTo(HttpStatus.FOUND);
                
                // Body is empty on redirect; redirect is enough to prove success path
                
                if (foundItem.isEmpty()) {
                        // New type item was created
                        Optional<Item> newItem = itemRepository.findByAllCharacteristics("Vestido Azul", "Seda", "Azul", "Zara", "F", "Vestido", "Curto", seededShopId);
                        Assertions.assertThat(newItem)
                                .as("A new item should be created")
                                .isPresent();

                        foundItem = newItem;
                }

                Item item = foundItem.get();

                // Verify the new instance of itemSingle was created
                List<ItemSingle> finalItemSingles = itemSingleRepository.findByItem_ItemId(item.getItemId());
                Integer finalCount = finalItemSingles.size();

                Assertions.assertThat(finalItemSingles)
                        .as("ItemSingle instances for the item should not be empty")
                        .isNotEmpty();

                if (itemSingles.isEmpty()) {
                        Assertions.assertThat(finalItemSingles.get(0).getSize())
                                .as("ItemSingle instance should be created for the item")
                                .isEqualTo("M"); 
                }
                else {
                        List<ItemSingle> newItem = new ArrayList<>(finalItemSingles);
                        newItem.removeAll(itemSingles);

                        // Size of item
                        String size = newItem.get(0).getSize();
                        Assertions.assertThat(size)
                                .as("ItemSingle instance should be created with correct size")
                                .isEqualTo("M");
                }
                
                Assertions.assertThat(finalCount)
                        .as("ItemSingle instance count should not decrease")
                        .isGreaterThanOrEqualTo(initialCount);
                

        }

        @Test
        @Requirement("SCRUM-8")
        @DisplayName("POST /magiclook/staff/item → invalid size returns error on dashboard")
        void addItem_invalidSize_showsErrorOnDashboard() {
                String sessionCookie = loginAsStaff(seededUsername, seededPassword);
                
                Optional<Item> foundItem = itemRepository.findByAllCharacteristics("Vestido Azul", "Seda", "Azul", "Zara", "F", "Vestido", "Curto", seededShopId);
                
                Integer initialCount = 0;
                if (foundItem.isPresent()) {
                        // Get initial itemSingle instances for that item
                        Item item = foundItem.get();
                        initialCount = itemSingleRepository.findByItem_ItemId(item.getItemId()).size();
                }

                MultiValueMap<String, Object> override = new LinkedMultiValueMap<>();
                override.add("size", "XXXL");

                ResponseEntity<String> response = restTemplate.exchange(
                        url("/magiclook/staff/item"),
                        HttpMethod.POST,
                        multipartBody(false, override, sessionCookie),
                        String.class);
                
                if (foundItem.isPresent()) {
                        Item item = foundItem.get();
                        // Verify no new instance of itemSingle was created
                        Integer finalCount = itemSingleRepository.findByItem_ItemId(item.getItemId()).size();

                        Assertions.assertThat(finalCount)
                                .as("ItemSingle instance should NOT be created on validation error")
                                .isEqualTo(initialCount);
                }

                else {
                        // Item didn't exist before, so no instances should exist now
                        Optional<Item> postAttemptItem = itemRepository.findByAllCharacteristics("Vestido Azul", "Seda", "Azul", "Zara", "F", "Vestido", "Curto", seededShopId);
                        Assertions.assertThat(postAttemptItem)
                                .as("Item should NOT be created on validation error")
                                .isNotPresent();
                }
        }

        @Test
        @Requirement("SCRUM-8")
        @DisplayName("POST /magiclook/staff/item → not logged in redirects to login")
        void addItem_notLoggedIn_redirectsToLogin() {
                // Don't login - use fresh client
                TestRestTemplate anonClient = new TestRestTemplate();
                
                ResponseEntity<String> response = anonClient.postForEntity(
                        url("/magiclook/staff/item"), 
                        multipartBody(false, null, null), 
                        String.class);

                Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
                Assertions.assertThat(response.getHeaders().getLocation())
                        .isNotNull();
                Assertions.assertThat(response.getHeaders().getLocation().getPath().replaceAll(";jsessionid=.*", ""))
                        .isEqualTo("/magiclook/staff/login");
        }

        @Test
        @Requirement("SCRUM-8")
        @DisplayName("POST /magiclook/staff/item → with image saves to filesystem")
        void addItem_withImage_savesImageFile() {
                String sessionCookie = loginAsStaff(seededUsername, seededPassword);

                // Ensure base item exists before uploading image
                Item ensuredItem = ensureItemExists();
                Optional<Item> foundItem = Optional.of(ensuredItem);
                
                // 2. Get initial itemSingle instances for that item
                Integer initialCount = 0;
                List<ItemSingle> itemSingles = List.of();
                if (foundItem.isPresent()) {

                        Item item = foundItem.get();
                        
                        itemSingles = itemSingleRepository.findByItem_ItemId(item.getItemId());
                        initialCount = itemSingles.size();

                }
                
                ResponseEntity<String> response = restTemplate.postForEntity(
                        url("/magiclook/staff/item"), 
                        multipartBody(true, null, sessionCookie), 
                        String.class);

                Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
                
                if (foundItem.isEmpty()) {
                        // New type item was created
                        Optional<Item> newItem = itemRepository.findByAllCharacteristics("Vestido Azul", "Seda", "Azul", "Zara", "F", "Vestido", "Curto", seededShopId);
                        Assertions.assertThat(newItem)
                                .as("A new item should be created")
                                .isPresent();

                        foundItem = newItem;
                }

                Item item = itemRepository.findById(foundItem.get().getItemId()).orElseThrow();

                String imagePath = item.getImagePath();
                Assertions.assertThat(imagePath)
                        .as("Image path should be set on the item")
                        .isNotNull()
                        .isNotEmpty();

                // Verify the new instance of itemSingle was created
                List<ItemSingle> finalItemSingles = itemSingleRepository.findByItem_ItemId(item.getItemId());
                Integer finalCount = finalItemSingles.size();

                Assertions.assertThat(finalItemSingles)
                        .as("ItemSingle instances for the item should not be empty")
                        .isNotEmpty();

                if (itemSingles.isEmpty()) {
                        Assertions.assertThat(finalItemSingles.get(0).getSize())
                                .as("ItemSingle instance should be created for the item")
                                .isEqualTo("M"); 
                }
                else {
                        List<ItemSingle> newItem = new ArrayList<>(finalItemSingles);
                        newItem.removeAll(itemSingles);

                        // Size of item
                        String size = newItem.get(0).getSize();
                        Assertions.assertThat(size)
                                .as("ItemSingle instance should be created with correct size")
                                .isEqualTo("M");
                }
                
                Assertions.assertThat(finalCount)
                        .as("ItemSingle instance should be created for the item")
                        .isEqualTo(initialCount + 1);
        }

        @Test
        @Requirement("SCRUM-10")
        @DisplayName("GET /magiclook/staff/item → lists items for logged-in staff")
        void getItems_loggedIn_returnsItemsPage() {
                        String sessionCookie = loginAsStaff(seededUsername, seededPassword);

                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.COOKIE, sessionCookie);

                ResponseEntity<String> response = restTemplate.exchange(
                                url("/magiclook/staff/item"), HttpMethod.GET, new HttpEntity<>(headers), String.class);

                Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                Assertions.assertThat(response.getBody())
                                .contains("Itens da Loja")
                                .contains("MagicLook Staff");
        }

        @Test
        @Requirement("SCRUM-10")
        @DisplayName("GET /magiclook/staff/item/{id} → shows item details for logged-in staff")
        void getItemDetails_loggedIn_returnsDetailsPage() {
                String sessionCookie = loginAsStaff(seededUsername, seededPassword);

                Item item = ensureItemExists();

                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.COOKIE, sessionCookie);

                ResponseEntity<String> response = restTemplate.exchange(
                                url("/magiclook/staff/item/" + item.getItemId()), HttpMethod.GET, new HttpEntity<>(headers), String.class);

                Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                Assertions.assertThat(response.getBody())
                                .contains(item.getName())
                                .contains("Detalhes do Item");
        }

        // Helper methods
        private String loginAsStaff(String username, String password) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

                MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
                form.add("usernameOrEmail", username);
                form.add("password", password);

                // Use a redirect-disabled RestTemplate to capture Set-Cookie from the initial 302
                CloseableHttpClient httpClient = HttpClients.custom()
                        .disableRedirectHandling()
                        .build();
                RestTemplate noRedirect = new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));

                ResponseEntity<Void> response = noRedirect.exchange(
                        url("/magiclook/staff/login"),
                        HttpMethod.POST,
                        new HttpEntity<>(form, headers),
                        Void.class);

                String raw = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
                if (raw == null) {
                        return "";
                }
                // Only keep the JSESSIONID pair, drop attributes
                return raw.split(";", 2)[0];
        }

        private HttpEntity<MultiValueMap<String, Object>> multipartBody(
                boolean includeImage, 
                MultiValueMap<String, Object> overrides,
                String sessionCookie) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                if (sessionCookie != null && !sessionCookie.isBlank()) {
                        headers.add(HttpHeaders.COOKIE, sessionCookie);
                }

                LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("name", "Vestido Azul");
                body.add("brand", "Zara");
                body.add("material", "Seda");
                body.add("color", "Azul");
                body.add("size", "M");
                body.add("priceRent", new BigDecimal("250.00").toString());
                body.add("priceSale", new BigDecimal("5000.00").toString());
                body.add("gender", "F");
                body.add("category", "Vestido");
                body.add("subcategory", "Curto");
                body.add("shop", String.valueOf(seededShopId));

                if (includeImage) {
                Resource image = new ByteArrayResource("fake image content".getBytes()) {
                        @Override
                        public String getFilename() {
                        return "vestido.jpg";
                        }
                };
                body.add("image", image);
                }

                if (overrides != null) {
                overrides.forEach((k, values) -> {
                        body.remove(k);
                        values.forEach(v -> body.add(k, v));
                });
                }

                return new HttpEntity<>(body, headers);
        }

        private URI url(String path) {
                return URI.create("http://localhost:" + port + path);
        }

        // Ensure there is at least one item associated with the seeded shop for detail tests
        private Item ensureItemExists() {
                Optional<Item> foundItem = itemRepository.findByAllCharacteristics(
                        "Vestido Azul", "Seda", "Azul", "Zara", "F", "Vestido", "Curto", seededShopId);

                if (foundItem.isPresent()) {
                        return foundItem.get();
                }

                ItemDTO itemDTO = new ItemDTO(
                        "Vestido Azul",
                        "Seda",
                        "Azul",
                        "Zara",
                        new BigDecimal("250.00"),
                        new BigDecimal("5000.00"),
                        seededShopId,
                        "F",
                        "Vestido",
                        "Curto"
                );

                int result = staffService.addItem(itemDTO, "M");
                Assertions.assertThat(result).isZero();

                return itemRepository.findByAllCharacteristics(
                        "Vestido Azul", "Seda", "Azul", "Zara", "F", "Vestido", "Curto", seededShopId)
                        .orElseThrow();
        }
}