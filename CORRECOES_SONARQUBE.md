# Sugestões de Correção - Código Pronto para Implementar

Este documento contém código pronto para corrigir os problemas identificados.

---

## 1. CORRIGIR SECURITY - Senhas em Plaintext

### 1.1 Criar Configuração de Password Encoder

**Arquivo**: `src/main/java/com/magiclook/config/SecurityConfig.java` (NOVO)

```java
package com.magiclook.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 1.2 Atualizar UserService

**Arquivo**: `src/main/java/com/magiclook/service/UserService.java`

```java
package com.magiclook.service;

import com.magiclook.dto.UserRegistrationDTO;
import com.magiclook.data.User;
import com.magiclook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;

@Service
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public User register(UserRegistrationDTO dto) {
        // Validar entrada
        if (dto == null || dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username não pode estar vazio");
        }
        
        if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Senha não pode estar vazia");
        }
        
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email não pode estar vazio");
        }
        
        // Validar se as passwords coincidem
        if (!dto.passwordsMatch()) {
            logger.warn("Tentativa de registro com senhas não coincidentes para: {}", dto.getUsername());
            throw new IllegalArgumentException("As palavras-passe não coincidem");
        }
        
        // Verifica se username já existe
        if (userRepository.existsByUsername(dto.getUsername())) {
            logger.warn("Username já em uso: {}", dto.getUsername());
            throw new IllegalArgumentException("Username já está em uso");
        }
        
        // Verifica se email já existe
        if (userRepository.existsByEmail(dto.getEmail())) {
            logger.warn("Email já em uso: {}", dto.getEmail());
            throw new IllegalArgumentException("Email já está em uso");
        }
        
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // ✅ CORRIGIDO
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setTelephone(dto.getTelephone());
        
        User savedUser = userRepository.save(user);
        logger.info("Utilizador registado com sucesso: {}", user.getUsername());
        
        return savedUser;
    }
    
    public Optional<User> login(String username, String password) {
        // Validar entrada
        if (username == null || username.trim().isEmpty()) {
            logger.warn("Tentativa de login com username vazio");
            return Optional.empty();
        }
        
        if (password == null || password.isEmpty()) {
            logger.warn("Tentativa de login com password vazia");
            return Optional.empty();
        }
        
        User user = userRepository.findByUsername(username);
        
        if (user == null) {
            user = userRepository.findByEmail(username);
        }
        
        // Usar passwordEncoder.matches() ao invés de .equals() ✅ CORRIGIDO
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            logger.info("Login bem-sucedido para: {}", username);
            return Optional.of(user);
        }
        
        logger.warn("Falha de login para: {}", username);
        return Optional.empty(); // Retorna Optional ao invés de null ✅ CORRIGIDO
    }
}
```

### 1.3 Atualizar StaffService

```java
public Staff login(String usernameOrEmail, String password) {
    if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
        logger.warn("Tentativa de login staff com credenciais vazias");
        return null;
    }
    
    // Tentar encontrar por email primeiro
    Optional<Staff> staffByEmail = staffRepository.findByEmail(usernameOrEmail);
    if (staffByEmail.isPresent()) {
        Staff staff = staffByEmail.get();
        // ✅ USAR passwordEncoder.matches()
        if (passwordEncoder.matches(password, staff.getPassword())) {
            logger.info("Staff login bem-sucedido: {}", usernameOrEmail);
            return staff;
        }
    }

    // Se não encontrou por email, tentar por username
    Optional<Staff> staffByUsername = staffRepository.findByUsername(usernameOrEmail);
    if (staffByUsername.isPresent()) {
        Staff staff = staffByUsername.get();
        // ✅ USAR passwordEncoder.matches()
        if (passwordEncoder.matches(password, staff.getPassword())) {
            logger.info("Staff login bem-sucedido: {}", usernameOrEmail);
            return staff;
        }
    }

    logger.warn("Falha de login staff para: {}", usernameOrEmail);
    return null;
}
```

### 1.4 Atualizar DatabaseLoader

```java
// Remover senhas hardcoded ou usar propriedades
@Value("${app.default-user-password:temporaryPassword123}")
private String defaultUserPassword;

@Value("${app.default-staff-password:temporaryPassword123}")
private String defaultStaffPassword;

@Autowired
private PasswordEncoder passwordEncoder;

private void initDatabase() {
    // Init users
    if (userRepository.count() == 0) {
        User user1 = new User("Maria", "Silva", "maria@gmail.com", "911991911", 
            passwordEncoder.encode("maria?"), "maria");
        // ...
    }
}
```

---

## 2. CORRIGIR ItemService - System.out.println

**Arquivo**: `src/main/java/com/magiclook/service/ItemService.java`

### ANTES:
```java
public Item getItemById(Integer itemId) {
    if (itemId == null) {
        return null;
    }
    try {
        return itemRepository.findById(itemId).orElse(null);
    } catch (Exception e) {
        System.err.println("Erro ao buscar item com ID " + itemId + ": " + e.getMessage());
        return null;
    }
}
```

### DEPOIS:
```java
private static final Logger logger = LoggerFactory.getLogger(ItemService.class);

public Optional<Item> getItemById(Integer itemId) {
    if (itemId == null) {
        logger.warn("getItemById chamado com itemId=null");
        return Optional.empty();
    }
    
    try {
        return itemRepository.findById(itemId);
    } catch (Exception e) {
        logger.error("Erro ao buscar item com ID: {}", itemId, e);
        return Optional.empty();
    }
}

// Usar em controllers:
Item item = itemService.getItemById(itemId)
    .orElseThrow(() -> new ItemNotFoundException("Item não encontrado"));
```

---

## 3. CORRIGIR StaffController - NullPointerException

**Arquivo**: `src/main/java/com/magiclook/boundary/StaffController.java`

### ANTES:
```java
@PostMapping("/login")
public String staffLogin(...) {
    Staff staff = staffService.login(usernameOrEmail, password);

    if (staff != null) {
        session.setAttribute("loggedInStaff", staff);
        session.setAttribute("shopId", staff.getShop().getShopId());
        session.setAttribute("shopName", staff.getShop().getName());
```

### DEPOIS:
```java
@PostMapping("/login")
public String staffLogin(@RequestParam String usernameOrEmail,
        @RequestParam String password,
        HttpSession session,
        Model model) {

    Staff staff = staffService.login(usernameOrEmail, password);

    if (staff != null) {
        // ✅ VALIDAR QUE STAFF TEM SHOP
        if (staff.getShop() == null) {
            logger.error("Staff {} não tem shop associada", staff.getStaffId());
            model.addAttribute("error", "Erro de configuração: Staff sem loja associada");
            return STAFF_LOGIN_VIEW;
        }
        
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
```

---

## 4. CORRIGIR Exception Handling - StaffController

### ANTES:
```java
} catch (Exception e) {
    model.addAttribute(ERROR, "Erro ao adicionar item: " + e.getMessage());
    return STAFF_DASHBOARD_VIEW;
}
```

### DEPOIS:
```java
} catch (IOException e) {
    logger.error("Erro ao salvar imagem para item {}", itemDTO.getItemId(), e);
    model.addAttribute(ERROR, "Erro ao salvar imagem. Verifique o arquivo");
    return STAFF_DASHBOARD_VIEW;
} catch (IllegalArgumentException e) {
    logger.warn("Validação falhou ao adicionar item: {}", e.getMessage());
    model.addAttribute(ERROR, "Validação falhou: " + e.getMessage());
    return STAFF_DASHBOARD_VIEW;
} catch (RuntimeException e) {
    logger.error("Erro ao adicionar item", e);
    model.addAttribute(ERROR, "Erro ao adicionar item. Verifique os dados");
    return STAFF_DASHBOARD_VIEW;
}
```

---

## 5. CORRIGIR BookingService - Race Condition

### ANTES:
```java
private static final Object GLOBAL_BOOKING_LOCK = new Object();

public Booking createBooking(BookingRequestDTO bookingRequest, User user) {
    synchronized (GLOBAL_BOOKING_LOCK) {
        return doCreateBooking(bookingRequest, user);
    }
}
```

### DEPOIS:
```java
@Service
@Transactional(isolation = Isolation.SERIALIZABLE) // ✅ DATABASE HANDLES CONCURRENCY
public class BookingService {
    
    // Remover locks globais
    
    public Booking createBooking(BookingRequestDTO bookingRequest, User user) {
        // A transação com isolation=SERIALIZABLE garante que não há race conditions
        return doCreateBooking(bookingRequest, user);
    }
}
```

---

## 6. CORRIGIR DatabaseLoader - Refatorar Método Longo

### ANTES:
```java
@PostConstruct
void initDatabase() {
    // 240 linhas tudo junto
    // Init shops
    // Init users
    // Init staff
    // Init items
    // Init bookings
}
```

### DEPOIS:
```java
private static final Logger logger = LoggerFactory.getLogger(DatabaseLoader.class);

@PostConstruct
void initDatabase() {
    try {
        initShops();
        initUsers();
        initStaff();
        initItemTypes();
        initItems();
        logger.info("Base de dados inicializada com sucesso");
    } catch (Exception e) {
        logger.error("Erro ao inicializar base de dados", e);
        throw new RuntimeException("Falha ao inicializar base de dados", e);
    }
}

private void initShops() {
    if (shopRepository.count() == 0) {
        Shop shop1 = new Shop("Porto", "Porto");
        Shop shop2 = new Shop("Lisboa", "Lisboa");
        shopRepository.saveAll(Arrays.asList(shop1, shop2));
        shopRepository.flush();
    }
}

private void initUsers() {
    if (userRepository.count() == 0) {
        User user1 = new User("Maria", "Silva", "maria@gmail.com", "911991911", 
            passwordEncoder.encode("senha_segura_123"), "maria");
        // ...
        userRepository.saveAll(users);
        userRepository.flush();
    }
}

private void initStaff() {
    if (staffRepository.count() == 0 && shopRepository.count() > 0) {
        Shop shop1 = shopRepository.findByNameAndLocation("Porto", "Porto")
            .orElseThrow(() -> new IllegalStateException("Shop Porto não existe"));
        // ...
    }
}
```

---

## 7. CORRIGIR Validações em Entidades

### User.java
```java
@Entity
@Table(name = "app_user")
public class User implements Serializable {
    
    @NotBlank(message = "Email não pode estar vazio")
    @Email(message = "Email deve ser válido")
    @Column(unique = true)
    private String email;

    @Pattern(regexp = "^\\d{9,15}$", message = "Telefone deve ter 9-15 dígitos")
    private String telephone;

    @NotBlank(message = "Senha não pode estar vazia")
    @Length(min=8, max=60, message = "Senha deve ter entre 8 e 60 caracteres")
    @Column(nullable = false, length = 60)
    private String password;
    
    @NotBlank(message = "Username não pode estar vazio")
    @Length(min=3, max=50, message = "Username deve ter entre 3 e 50 caracteres")
    @Column(unique = true)
    private String username;
}
```

### Item.java
```java
@Entity
@Table(name = "item")
public class Item implements Serializable {
    
    @NotBlank(message = "Nome do item não pode estar vazio")
    private String name;
    
    @NotNull(message = "Preço de aluguel não pode ser nulo")
    @DecimalMin(value = "0.00", message = "Preço deve ser positivo")
    @Digits(integer=10, fraction=2)
    private BigDecimal priceRent;
    
    @NotNull(message = "Preço de venda não pode ser nulo")
    @DecimalMin(value = "0.00", message = "Preço deve ser positivo")
    @Digits(integer=10, fraction=2)
    private BigDecimal priceSale;
    
    @NotNull(message = "Loja não pode ser nula")
    @ManyToOne
    @JoinColumn(name = "shop_id")
    private Shop shop;
}
```

### Booking.java
```java
@Entity
@Table(name = "booking")
public class Booking implements Serializable {
    
    @NotNull(message = "Data de início não pode ser nula")
    @Column(name = "start_use_date")
    private Date startUseDate;

    @NotNull(message = "Data de fim não pode ser nula")
    @Column(name = "end_use_date")
    private Date endUseDate;

    @NotNull(message = "Preço total não pode ser nulo")
    @DecimalMin(value = "0.00", message = "Preço deve ser positivo")
    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @AssertTrue(message = "Data de início deve ser antes da data de fim")
    public boolean isValidDateRange() {
        if (startUseDate == null || endUseDate == null) return true;
        return startUseDate.before(endUseDate);
    }
}
```

---

## 8. CRIAR CUSTOM EXCEPTIONS

**Arquivo**: `src/main/java/com/magiclook/exception/ItemNotFoundException.java` (NOVO)

```java
package com.magiclook.exception;

public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException(String message) {
        super(message);
    }
    
    public ItemNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

**Arquivo**: `src/main/java/com/magiclook/exception/ItemNotAvailableException.java` (NOVO)

```java
package com.magiclook.exception;

public class ItemNotAvailableException extends RuntimeException {
    public ItemNotAvailableException(String message) {
        super(message);
    }
}
```

**Arquivo**: `src/main/java/com/magiclook/exception/InvalidBookingException.java` (NOVO)

```java
package com.magiclook.exception;

public class InvalidBookingException extends RuntimeException {
    public InvalidBookingException(String message) {
        super(message);
    }
}
```

---

## 9. CONVERTER Date PARA LocalDateTime

### ANTES (StaffService):
```java
Calendar calendar = Calendar.getInstance();
calendar.setTime(startDate);
calendar.add(Calendar.DAY_OF_MONTH, -1);
Date pickupDate = calendar.getTime();
```

### DEPOIS:
```java
ZonedDateTime startZoned = startUseDate.toInstant()
    .atZone(ZoneId.systemDefault());
ZonedDateTime pickupZoned = startZoned.minusDays(1);
Date pickupDate = Date.from(pickupZoned.toInstant());
```

Ou melhor ainda, use LocalDate/LocalDateTime em toda a aplicação:

```java
LocalDate startDate = startUseDate.toInstant()
    .atZone(ZoneId.systemDefault())
    .toLocalDate();

LocalDate pickupDate = startDate.minusDays(1);
```

---

## 10. REFATORAR MÉTODOS LONGOS

### Exemplo: StaffController.addItem

```java
@PostMapping("/item")
@Timed(value = "request.addItem")
public String addItem(
        @Valid ItemAddRequest request,
        HttpSession session,
        Model model) {
    
    try {
        Staff staff = getStaffFromSession(session);
        if (staff == null) {
            return "redirect:/magiclook/staff/login";
        }
        
        validateItemRequest(request);
        ItemDTO itemDTO = convertToDTO(request);
        
        int result = staffService.addItem(itemDTO, request.getSize());
        
        if (result != 0) {
            return handleAddItemError(result, model);
        }
        
        handleImageUpload(request.getImage(), itemDTO.getItemId());
        
        logger.info("Item adicionado com sucesso: {}", itemDTO.getItemId());
        return "redirect:/magiclook/staff/dashboard";
        
    } catch (IllegalArgumentException e) {
        logger.warn("Validação falhou ao adicionar item: {}", e.getMessage());
        model.addAttribute(ERROR, "Validação: " + e.getMessage());
        return STAFF_DASHBOARD_VIEW;
    } catch (Exception e) {
        logger.error("Erro ao adicionar item", e);
        model.addAttribute(ERROR, "Erro ao adicionar item");
        return STAFF_DASHBOARD_VIEW;
    }
}

private Staff getStaffFromSession(HttpSession session) {
    return (Staff) session.getAttribute("loggedInStaff");
}

private void validateItemRequest(ItemAddRequest request) {
    if (request == null) throw new IllegalArgumentException("Pedido inválido");
    if (request.getName() == null || request.getName().isEmpty()) 
        throw new IllegalArgumentException("Nome do item obrigatório");
}

private ItemDTO convertToDTO(ItemAddRequest request) {
    return new ItemDTO(
        request.getName(),
        request.getMaterial(),
        request.getColor(),
        request.getBrand(),
        request.getPriceRent(),
        request.getPriceSale(),
        request.getShop(),
        request.getGender(),
        request.getCategory(),
        request.getSubcategory()
    );
}

private String handleAddItemError(int resultCode, Model model) {
    String message = switch(resultCode) {
        case -1 -> "Tamanho inválido";
        case -2 -> "Material inválido";
        case -3 -> "Shop ou tipo de item inválido";
        default -> "Erro desconhecido";
    };
    model.addAttribute(ERROR, message);
    return STAFF_DASHBOARD_VIEW;
}

private void handleImageUpload(MultipartFile image, Integer itemId) throws IOException {
    if (image != null && !image.isEmpty()) {
        String imagePath = staffService.saveImage(image, itemId);
        staffService.updateItemImage(itemId, imagePath);
    }
}
```

---

## 11. USAR LOGGER EM TODOS OS SERVICES

```java
private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

public Booking createBooking(BookingRequestDTO bookingRequest, User user) {
    logger.debug("Iniciando criação de reserva para utilizador: {}", user.getUserId());
    
    try {
        validateBookingRequest(bookingRequest);
        
        Booking booking = doCreateBooking(bookingRequest, user);
        
        logger.info("Reserva criada com sucesso. ID: {}, User: {}, Item: {}", 
            booking.getBookingId(), user.getUserId(), bookingRequest.getItemId());
        
        return booking;
    } catch (ItemNotAvailableException e) {
        logger.warn("Item não disponível na criação de reserva: {}", e.getMessage());
        throw e;
    } catch (Exception e) {
        logger.error("Erro ao criar reserva para utilizador: {}", user.getUserId(), e);
        throw new RuntimeException("Falha ao criar reserva", e);
    }
}
```

---

## 12. CRIAR UTIL PARA VALIDAÇÕES COMUNS

**Arquivo**: `src/main/java/com/magiclook/util/ValidationUtil.java` (NOVO)

```java
package com.magiclook.util;

import java.util.regex.Pattern;

public class ValidationUtil {
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^\\d{9,15}$");
    
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }
    
    public static void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " não pode estar vazio");
        }
    }
    
    public static void validateEmail(String email) {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Email inválido: " + email);
        }
    }
    
    public static void validateDateRange(java.util.Date start, java.util.Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Datas não podem ser nulas");
        }
        if (!start.before(end)) {
            throw new IllegalArgumentException("Data de início deve ser antes de data de fim");
        }
    }
}
```

---

## RESUMO DAS ALTERAÇÕES

| Arquivo | Tipo | Prioridade |
|---------|------|-----------|
| SecurityConfig.java | NOVO | CRÍTICA |
| UserService.java | REFACTOR | CRÍTICA |
| StaffService.java | REFACTOR | CRÍTICA |
| ItemService.java | REFACTOR | ALTA |
| StaffController.java | REFACTOR | ALTA |
| BookingService.java | REFACTOR | ALTA |
| DatabaseLoader.java | REFACTOR | ALTA |
| Todas as Entities | VALIDAÇÃO | MÉDIA |

---

**Total estimado de mudanças**: ~2000 linhas de código novo/modificado
**Esforço**: 40-60 horas
