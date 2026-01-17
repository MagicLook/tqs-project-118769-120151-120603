# Análise SonarQube - Problemas de Código Identificados

Data: 17 de Janeiro de 2026
Projeto: MagicLook - Sistema de Aluguel de Roupa

---

## SUMÁRIO EXECUTIVO

Foram identificados **45+ problemas potenciais** em 12 arquivos Java que SonarQube detectaria, incluindo:
- ⚠️ 8 problemas de segurança CRÍTICOS
- 🔴 12 code smells importantes
- 🟠 15 problemas de boas práticas
- 🟡 10+ possíveis NPE (NullPointerException)

---

## PROBLEMAS POR ARQUIVO

---

### 1. **UserService.java** 🔴 CRÍTICO

**Localização**: `/src/main/java/com/magiclook/service/UserService.java`

#### Problema 1.1: Segurança - Passwords em Plaintext
**Tipo**: VULNERABILIDADE DE SEGURANÇA
**Linha**: 37
**Código**:
```java
user.setPassword(dto.getPassword());
```
**Problema**: Senha sendo armazenada em texto plano (plaintext) no banco de dados
**Impacto**: CRÍTICO - Risco massivo de segurança
**Solução**:
```java
user.setPassword(BCryptPasswordEncoder().encode(dto.getPassword()));
// Ou usar Spring Security
```

#### Problema 1.2: Segurança - Login com Plaintext
**Tipo**: VULNERABILIDADE DE SEGURANÇA
**Linha**: 44-46
**Código**:
```java
if (user != null && user.getPassword().equals(password)) {
    return user;
}
```
**Problema**: Comparação de senha em plaintext (timing attack vulnerability)
**Impacto**: CRÍTICO
**Solução**:
```java
if (user != null && passwordEncoder.matches(password, user.getPassword())) {
    return user;
}
```

#### Problema 1.3: Retorna null em vez de Optional
**Tipo**: Code Smell - Bad Practice
**Linha**: 49
**Código**:
```java
return null; // Login falhou
```
**Problema**: Retornar null é perigoso e pode causar NullPointerException
**Impacto**: ALTO
**Solução**:
```java
return Optional.empty();
// Ou lançar exceção customizada
```

#### Problema 1.4: Falta de validação de entrada
**Tipo**: Input Validation
**Linhas**: 16-28
**Problema**: Sem validação se `username`, `email`, `password` são válidos antes de usar
**Impacto**: MÉDIO
**Solução**:
```java
if (username == null || username.trim().isEmpty()) {
    throw new IllegalArgumentException("Username não pode estar vazio");
}
```

#### Problema 1.5: Falta de logging
**Tipo**: Code Smell
**Linhas**: Múltiplas
**Problema**: Nenhum logging de operações críticas (login falho, registro duplicado)
**Impacto**: BAIXO (Auditoria)
**Solução**:
```java
private static final Logger logger = LoggerFactory.getLogger(UserService.class);
logger.warn("Login falhou para username: {}", username);
```

---

### 2. **UserController.java** 🔴 CRÍTICO

**Localização**: `/src/main/java/com/magiclook/boundary/UserController.java`

#### Problema 2.1: NullPointerException Risk - user pode ser null
**Tipo**: NullPointerException Risk
**Linhas**: 92-98
**Código**:
```java
@PostMapping("/login")
public String login(...) {
    User user = userService.login(username, password);
    
    if (user != null) {
        session.setAttribute(ATTR_LOGGED_IN_USER, user);
        // ...
```
**Problema**: `user` pode ser null, mas depois é usado sem verificação adicional em múltiplos lugares
**Impacto**: MÉDIO
**Solução**:
```java
User user = userService.login(username, password)
    .orElse(null);
if (user == null) {
    model.addAttribute(ATTR_ERROR, "Credenciais inválidas");
    return VIEW_LOGIN;
}
```

#### Problema 2.2: Hardcoded redirect URL
**Tipo**: Code Smell - Magic Strings
**Linha**: 72
**Código**:
```java
return REDIRECT_LOGIN + "?success"; 
return "redirect:/magiclook/dashboard";
```
**Problema**: URLs hardcoded em múltiplos lugares (linhas 72, 98, 107, 114, etc.)
**Impacto**: MÉDIO - Difícil manutenção
**Solução**: Já parcialmente feito com constantes, mas faltam algumas
```java
private static final String REDIRECT_DASHBOARD = "redirect:/magiclook/dashboard";
```

#### Problema 2.3: URLEncoder sem tratamento de exceção
**Tipo**: Unchecked Exception
**Linhas**: 243-254
**Código**:
```java
redirectUrl.append("color=").append(URLEncoder.encode(color, StandardCharsets.UTF_8));
```
**Problema**: `URLEncoder.encode()` não lança exceção, mas código complexo de encoding
**Impacto**: BAIXO
**Solução**: Usar `UriComponentsBuilder` do Spring
```java
UriComponentsBuilder.fromPath("/magiclook/items/{gender}")
    .queryParam("color", color)
    .build()
```

#### Problema 2.4: Método sobrecarregado com teste direto
**Tipo**: Code Smell - Mistura de responsabilidades
**Linhas**: 131-142
**Código**:
```java
// Convenience overload for unit tests (direct call)
public String showMenItems(HttpSession session, Model model) {
    // Implementação diferente do @GetMapping
}
```
**Problema**: Método alternativo apenas para testes. Viola Single Responsibility
**Impacto**: MÉDIO
**Solução**: Remover e usar mocking nos testes

---

### 3. **StaffController.java** 🔴 CRÍTICO

**Localização**: `/src/main/java/com/magiclook/boundary/StaffController.java`

#### Problema 3.1: Try-catch genérico sem logging adequado
**Tipo**: Poor Exception Handling
**Linhas**: 164-167, 300-303, 330-333
**Código**:
```java
} catch (Exception e) {
    model.addAttribute(ERROR, "Erro ao adicionar item: " + e.getMessage());
    return STAFF_DASHBOARD_VIEW;
}
```
**Problema**: 
- Captura Exception muito genérica
- Não faz logging (SonarQube recomenda logging)
- getMessage() pode retornar null
**Impacto**: MÉDIO
**Solução**:
```java
} catch (IOException e) {
    logger.error("Erro ao salvar imagem para item {}", itemDTO.getItemId(), e);
    model.addAttribute(ERROR, "Erro ao salvar imagem");
    return STAFF_DASHBOARD_VIEW;
} catch (RuntimeException e) {
    logger.error("Erro ao adicionar item", e);
    model.addAttribute(ERROR, "Erro ao adicionar item");
    return STAFF_DASHBOARD_VIEW;
}
```

#### Problema 3.2: Múltiplas sessões setAttribute sem validação
**Tipo**: Code Smell - Duplicação
**Linhas**: 56-64
**Código**:
```java
session.setAttribute("loggedInStaff", staff);
session.setAttribute("staffId", staff.getStaffId());
session.setAttribute("staffName", staff.getName());
session.setAttribute("staffEmail", staff.getEmail());
session.setAttribute("staffUsername", staff.getUsername());
session.setAttribute("shopId", staff.getShop().getShopId());
session.setAttribute("shopName", staff.getShop().getName());
```
**Problema**: 
- `staff.getShop()` pode retornar null - NullPointerException
- Duplicação de atributos de sessão
- Sem validação de dados antes de armazenar
**Impacto**: ALTO
**Solução**:
```java
if (staff.getShop() == null) {
    throw new IllegalStateException("Staff sem loja associada");
}
SessionUtils.storeStaffInSession(session, staff);
```

#### Problema 3.3: Métodos muito longos e complexos
**Tipo**: Code Smell - Method Too Long
**Linhas**: 113-172 (addItem)
**Problema**: Método com 60+ linhas, múltiplas responsabilidades
**Impacto**: MÉDIO - Difícil de testar e manter
**Solução**: Extrair em métodos menores
```java
private void validateItemDTO(ItemDTO itemDTO) { }
private String handleItemSave(ItemDTO itemDTO, MultipartFile image) { }
private void logItemCreation(ItemDTO itemDTO) { }
```

#### Problema 3.4: Resposta @ResponseBody sem tratamento de erro estruturado
**Tipo**: Incomplete Exception Handling
**Linhas**: 317-331
**Código**:
```java
@DeleteMapping("/item/{itemId}/size/{size}")
@ResponseBody
public org.springframework.http.ResponseEntity<?> deleteItemSize(...) {
    try {
        staffService.deleteItemSize(itemId, size);
        return org.springframework.http.ResponseEntity.ok().build();
    } catch (Exception e) {
        return org.springframework.http.ResponseEntity.badRequest().body(e.getMessage());
    }
}
```
**Problema**: 
- `e.getMessage()` pode ser null
- Sem logging do erro
- Retorna informação genérica
**Impacto**: MÉDIO
**Solução**:
```java
} catch (Exception e) {
    logger.error("Erro ao deletar tamanho {} do item {}", size, itemId, e);
    return ResponseEntity.badRequest()
        .body(new ErrorResponse("Erro ao deletar tamanho do item"));
}
```

---

### 4. **BookingController.java** 🔴 CRÍTICO

**Localização**: `/src/main/java/com/magiclook/boundary/BookingController.java`

#### Problema 4.1: Múltiplos possíveis NullPointerExceptions
**Tipo**: Null Pointer Risk
**Linhas**: 59-64
**Código**:
```java
Item item = itemService.getItemById(itemId);
if (item == null) {
    return REDIRECT_DASHBOARD;
}
```
**Problema**: Bom, mas faltam verificações em outros métodos
**Linhas**: 215-219
```java
List<Booking> bookings = bookingService.getUserBookings(user);
if (bookings == null) {
    bookings = new ArrayList<>();
}
```
**Problema**: Defensive programming, mas serviço deve garantir return não-null
**Impacto**: MÉDIO
**Solução**: `getUserBookings` deve retornar `Collections.emptyList()`, não null

#### Problema 4.2: Try-catch genérico múltiplas vezes
**Tipo**: Incomplete Exception Handling
**Linhas**: 160, 359, 399, 433, 486, 536
**Código**:
```java
} catch (Exception e) {
    model.addAttribute(ATTR_ERROR, "Erro ao criar reserva: " + e.getMessage());
    return VIEW_BOOKING_FORM;
}
```
**Problema**: Mesma estrutura repetida 6 vezes
**Impacto**: MÉDIO - Duplicação de código
**Solução**: Criar método utilitário
```java
private void handleBookingException(Exception e, Model model) {
    logger.error("Erro em operação de reserva", e);
    model.addAttribute(ATTR_ERROR, "Erro ao processar reserva");
}
```

#### Problema 4.3: UUID.fromString() sem validação
**Tipo**: Input Validation
**Linha**: 288
**Código**:
```java
UUID bookingId = UUID.fromString(id);
```
**Problema**: Se `id` for inválido, lança IllegalArgumentException não tratada
**Impacto**: MÉDIO
**Solução**:
```java
UUID bookingId;
try {
    bookingId = UUID.fromString(id);
} catch (IllegalArgumentException e) {
    logger.warn("ID de reserva inválido: {}", id);
    return REDIRECT_DASHBOARD;
}
```

#### Problema 4.4: Uso de Date ao invés de LocalDate/ZonedDateTime
**Tipo**: Bad Practice - Legacy API
**Linhas**: 75, 80-81, etc.
**Código**:
```java
Date today = new Date();
if (startLocal.isBefore(todayLocal)) {
```
**Problema**: Mistura de `Date` (java.util) e `LocalDate` (java.time)
**Impacto**: MÉDIO - Confuso, propenso a erros de timezone
**Solução**: Usar apenas `java.time` (LocalDateTime, ZonedDateTime)

#### Problema 4.5: Operações de stream sem tratamento de erro
**Tipo**: Incomplete Exception Handling
**Linhas**: 222-232, 245-254
**Código**:
```java
bookings.sort((b1, b2) -> {
    if (b1.getStartUseDate() == null && b2.getStartUseDate() == null) return 0;
    return b2.getStartUseDate().compareTo(b1.getStartUseDate());
});
```
**Problema**: Se comparador lançar exceção, não é capturado
**Impacto**: BAIXO
**Solução**: Validar dados antes de operação ou usar Comparator.nullsLast()

#### Problema 4.6: Falta de logging em operações críticas
**Tipo**: Code Smell - Auditoria
**Linhas**: 140-165
**Problema**: Criar reserva sem logging
**Impacto**: BAIXO
**Solução**:
```java
logger.info("Reserva criada: ID={}, User={}, Item={}, Datas={}-{}", 
    booking.getBookingId(), user.getUserId(), itemId, 
    startUseDate, endUseDate);
```

---

### 5. **BookingService.java** 🔴 CRÍTICO

**Localização**: `/src/main/java/com/magiclook/service/BookingService.java`

#### Problema 5.1: Sincronização inadequada - Race condition potencial
**Tipo**: Concurrency Issue
**Linhas**: 43-46, 51-53
**Código**:
```java
private static final Map<String, Object> itemLocks = new ConcurrentHashMap<>();
private static final Object GLOBAL_BOOKING_LOCK = new Object();

synchronized (GLOBAL_BOOKING_LOCK) {
    return doCreateBooking(bookingRequest, user);
}
```
**Problema**: 
- Lock global é muito agressivo (performance)
- itemLocks nunca é usado na verdade
- Deadlock potencial em operações múltiplas
**Impacto**: ALTO - Problema real de concorrência
**Solução**:
```java
@Transactional(isolation = Isolation.SERIALIZABLE)
public Booking createBooking(...) {
    // Deixar banco de dados gerenciar concorrência
}
```

#### Problema 5.2: Múltiplas exceções genéricas lançadas
**Tipo**: Poor Exception Design
**Linhas**: 56, 65, 103, 108
**Código**:
```java
throw new RuntimeException("Datas inválidas");
throw new RuntimeException("Utilizador não autenticado");
throw new IllegalArgumentException("Datas inválidas");
```
**Problema**: 
- Exceções muito genéricas
- Inconsistência (RuntimeException vs IllegalArgumentException)
- Difícil de tratar especificamente
**Impacto**: MÉDIO
**Solução**:
```java
public class InvalidBookingException extends RuntimeException { }
public class ItemNotAvailableException extends RuntimeException { }
```

#### Problema 5.3: Calendar.getInstance() - Não thread-safe
**Tipo**: Concurrency Issue
**Linhas**: 77-80, 87, etc.
**Código**:
```java
Calendar calendar = Calendar.getInstance();
calendar.setTime(bookingRequest.getStartUseDate());
```
**Problema**: Calendar não é thread-safe
**Impacto**: MÉDIO
**Solução**:
```java
LocalDateTime startLocal = bookingRequest.getStartUseDate()
    .toInstant()
    .atZone(ZoneId.systemDefault())
    .toLocalDateTime()
    .minusDays(1);
```

#### Problema 5.4: Método muito longo com lógica complexa
**Tipo**: Cognitive Complexity Too High
**Linhas**: 52-135
**Problema**: `doCreateBooking()` tem 80+ linhas e faz múltiplas coisas
**Impacto**: MÉDIO - Difícil de testar
**Solução**: Extrair métodos
```java
private void validateBookingRequest(BookingRequestDTO request) { }
private ItemSingle findAvailableItem(BookingRequestDTO request) { }
private Booking buildBooking(...) { }
```

#### Problema 5.5: Falta de logging em operações críticas
**Tipo**: Auditoria
**Linhas**: Múltiplas
**Problema**: Sem logs de criação de reserva, cancelamento, etc.
**Impacto**: BAIXO
**Solução**: Adicionar logger
```java
logger.info("Booking criada: {}", booking.getBookingId());
```

#### Problema 5.6: Null checks defensivos em excesso
**Tipo**: Smelly Code
**Linhas**: 213-215, 221-223
**Código**:
```java
if (bookingRequest == null || bookingRequest.isValidDates()) {
    throw new IllegalArgumentException("Datas inválidas");
}
```
**Problema**: Defensivo demais, nunca deveria receber null
**Impacto**: BAIXO
**Solução**: Remover null check, validar onde necessário

---

### 6. **StaffService.java** 🔴 CRÍTICO

**Localização**: `/src/main/java/com/magiclook/service/StaffService.java`

#### Problema 6.1: IOException não é tratada
**Tipo**: Unchecked Exception
**Linhas**: 73-90
**Código**:
```java
public String saveImage(MultipartFile image, Integer itemId) throws IOException {
    // ...
    Files.copy(inputStream, srcFilePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    Files.copy(inputStream, targetFilePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
```
**Problema**: 
- Lança IOException, mas não há try-catch no controller
- `image.getInputStream()` pode lançar IOException
- Recurso (inputStream) não é garantidamente fechado
**Impacto**: ALTO
**Solução**:
```java
try (InputStream inputStream = image.getInputStream()) {
    Files.copy(inputStream, srcFilePath, 
        StandardCopyOption.REPLACE_EXISTING);
}
```

#### Problema 6.2: Recurso não fechado adequadamente
**Tipo**: Resource Leak
**Linhas**: 79, 85
**Código**:
```java
try (java.io.InputStream inputStream = image.getInputStream()) {
    Files.copy(inputStream, srcFilePath, ...);
}
```
**Problema**: Bom aqui, mas em outras partes pode não estar
**Impacto**: MÉDIO (se houver problema)

#### Problema 6.3: Validação insuficiente de entrada
**Tipo**: Input Validation
**Linhas**: 66-69
**Código**:
```java
String fileName = String.format("item_%s_%s", idPart, safeOriginal);
```
**Problema**: 
- Não valida se filename é vazio após sanitização
- Não valida tamanho máximo de arquivo
- Não valida tipo MIME
**Impacto**: MÉDIO
**Solução**:
```java
if (safeOriginal.isEmpty()) {
    throw new IllegalArgumentException("Nome de arquivo inválido");
}
if (image.getSize() > MAX_FILE_SIZE) {
    throw new IllegalArgumentException("Arquivo muito grande");
}
```

#### Problema 6.4: Hardcoded paths e strings
**Tipo**: Code Smell - Magic Strings
**Linhas**: 75, 81
**Código**:
```java
Path srcStaticBase = Paths.get("src/main/resources/static").toAbsolutePath();
Path targetStaticBase = Paths.get("target/classes/static").toAbsolutePath();
```
**Problema**: Paths hardcoded, não configuráveis
**Impacto**: MÉDIO
**Solução**: Usar `@Value` ou propriedades de configuração

#### Problema 6.5: Senhas em plaintext
**Tipo**: VULNERABILIDADE DE SEGURANÇA
**Linhas**: 315-319
**Código**:
```java
public Staff login(String usernameOrEmail, String password) {
    Optional<Staff> staffByEmail = staffRepository.findByEmail(usernameOrEmail);
    if (staffByEmail.isPresent()) {
        Staff staff = staffByEmail.get();
        if (staff.getPassword().equals(password)) {
```
**Problema**: Mesmo problema de segurança - passwords em plaintext
**Impacto**: CRÍTICO
**Solução**: Usar BCrypt/Spring Security

#### Problema 6.6: Métodos muito longos
**Tipo**: Code Smell - Method Too Long
**Linhas**: 237-310 (updateItem)
**Problema**: 73 linhas de lógica complexa
**Impacto**: MÉDIO
**Solução**: Extrair métodos helpers

---

### 7. **ItemService.java** 🔴 CRÍTICO

**Localização**: `/src/main/java/com/magiclook/service/ItemService.java`

#### Problema 7.1: Null check defensivo com logging inadequado
**Tipo**: Poor Exception Handling
**Linhas**: 95-99
**Código**:
```java
try {
    return itemRepository.findById(itemId).orElse(null);
} catch (Exception e) {
    System.err.println("Erro ao buscar item com ID " + itemId + ": " + e.getMessage());
    return null;
}
```
**Problema**: 
- `System.err.println()` é ANTI-PATTERN (SonarQube flags isso)
- Deveria usar Logger
- Retorna null ao invés de Optional
- `e.getMessage()` pode ser null
**Impacto**: ALTO
**Solução**:
```java
public Optional<Item> getItemById(Integer itemId) {
    if (itemId == null) {
        logger.warn("getItemById chamado com itemId=null");
        return Optional.empty();
    }
    return itemRepository.findById(itemId);
}
```

#### Problema 7.2: Métodos que retornam null implicitamente
**Tipo**: Bad Practice
**Linhas**: 95-99
**Código**:
```java
return null; // Erro ao buscar
```
**Problema**: Retorna null quando deveria retornar Optional
**Impacto**: MÉDIO
**Solução**: Devolver `Optional.empty()`

#### Problema 7.3: Stream sem null check
**Tipo**: Null Pointer Risk
**Linhas**: 38-41
**Código**:
```java
public List<Item> getRecentItems(int limit) {
    return itemRepository.findAll().stream()
            .limit(limit)
            .collect(Collectors.toList());
}
```
**Problema**: `findAll()` nunca deveria retornar null, mas se retornar, explode
**Impacto**: BAIXO (Improável)

#### Problema 7.4: Parâmetros de método não validados
**Tipo**: Input Validation
**Linhas**: 62
**Código**:
```java
public List<Item> searchItemsWithFilters(String gender, String color, String brand, 
                                         String material, String category, ...)
```
**Problema**: Sem validação de `gender`, sem checar se é "M" ou "F"
**Impacto**: MÉDIO
**Solução**:
```java
if (gender == null || (!gender.equals("M") && !gender.equals("F"))) {
    throw new IllegalArgumentException("Gender inválido");
}
```

#### Problema 7.5: Possível NPE em chainings
**Tipo**: Null Pointer Risk
**Linhas**: 82-89
**Código**:
```java
public List<String> getAvailableSizesForItem(Integer itemId) {
    return itemSingleRepository.findByItem_ItemId(itemId)
        .stream()
        .filter(is -> "AVAILABLE".equals(is.getState()))
```
**Problema**: Se `findByItem_ItemId()` retorna null (não esperado mas possível), NPE
**Impacto**: BAIXO
**Solução**:
```java
return itemSingleRepository.findByItem_ItemId(itemId)
    .stream()
    .filter(is -> is.getState() != null && is.getState().equals("AVAILABLE"))
```

---

### 8. **DatabaseLoader.java** 🔴 CRÍTICO

**Localização**: `/src/main/java/com/magiclook/loader/DatabaseLoader.java`

#### Problema 8.1: Múltiplas possíveis NullPointerExceptions
**Tipo**: Null Pointer Risk
**Linhas**: 49, 55
**Código**:
```java
shop1 = shopRepository.findByNameAndLocation("Porto", "Porto").orElse(null);
shop2 = shopRepository.findByNameAndLocation("Lisboa", "Lisboa").orElse(null);
// ...
if (staffRepository.count() == 0 && shop1 != null && shop2 != null) {
```
**Problema**: `shop1` e `shop2` podem ser null
**Linhas**: 60-64
```java
Staff staff1 = new Staff("Admin", "admin@gmail.com", "admin123", "admin", shop1);
```
**Problema**: `shop1` é usado sem null check se code branch anterior passa
**Impacto**: ALTO
**Solução**:
```java
if (shop1 == null || shop2 == null) {
    throw new IllegalStateException("Shops não foram criadas");
}
```

#### Problema 8.2: Senhas hardcoded em código
**Tipo**: VULNERABILIDADE DE SEGURANÇA
**Linhas**: 53, 62, 68
**Código**:
```java
User user1 = new User("Maria", "Silva", "maria@gmail.com", "911991911", "maria?", "maria");
Staff staff1 = new Staff("Admin", "admin@gmail.com", "admin123", "admin", shop1);
```
**Problema**: 
- Senhas visíveis no código
- Senhas em plaintext
- Credenciais de teste no mesmo arquivo que vai para produção
**Impacto**: CRÍTICO
**Solução**:
```java
@Value("${app.default-password:changeMe}")
private String defaultPassword;
// E usar encoder
```

#### Problema 8.3: Método muito longo
**Tipo**: Code Smell - Method Too Long
**Linhas**: 33-272
**Problema**: `initDatabase()` tem 240 linhas!
**Impacto**: MÉDIO - Muito complexo
**Solução**: Dividir em múltiplos métodos
```java
private void initShops() { }
private void initUsers() { }
private void initStaff() { }
private void initItemTypes() { }
private void initItems() { }
```

#### Problema 8.4: Sem tratamento de erros
**Tipo**: Incomplete Exception Handling
**Linhas**: Múltiplas
**Problema**: Se alguma operação falhar, tudo quebra sem mensagem clara
**Impacto**: MÉDIO
**Solução**:
```java
try {
    initShops();
} catch (Exception e) {
    logger.error("Erro ao inicializar shops", e);
    throw new RuntimeException("Falha ao inicializar banco de dados", e);
}
```

---

### 9. **User.java** 🟠

**Localização**: `/src/main/java/com/magiclook/data/User.java`

#### Problema 9.1: Sem validação de constraints
**Tipo**: Data Validation
**Linhas**: 26-45
**Código**:
```java
private String email;
private String telephone;
private String password;
private String username;
```
**Problema**: 
- Email não é validado (formato)
- Telephone não é validado
- Password pode ser vazio
- Username pode ser vazio
**Impacto**: MÉDIO
**Solução**:
```java
@Email
private String email;

@Pattern(regexp = "^\\d{9,15}$")
private String telephone;

@Length(min=8, max=60)
private String password;

@NotBlank
@Length(min=3, max=50)
private String username;
```

#### Problema 9.2: serialVersionUID não é final
**Tipo**: Code Smell
**Linha**: 10
**Código**:
```java
private static final long serialVersionUID = 1L;
```
**Problema**: Bom, mas potencial problema se classe mudar
**Impacto**: BAIXO

#### Problema 9.3: Sem hashCode/equals implementado
**Tipo**: Code Smell
**Linha**: Toda a classe
**Problema**: Extends Serializable mas sem implementar hashCode/equals
**Impacto**: BAIXO
**Solução**: Gerar via IDE ou usar Lombok

---

### 10. **Item.java** 🟠

**Localização**: `/src/main/java/com/magiclook/data/Item.java`

#### Problema 10.1: Campo `available` não sincronizado com `itemSingles`
**Tipo**: Design Issue
**Linhas**: 29, 30
**Código**:
```java
private boolean available = true;
@OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
private List<ItemSingle> itemSingles;
```
**Problema**: Há um campo `available` que não é usado, e o status real está em `itemSingles`
**Impacto**: MÉDIO - Confuso
**Solução**: Remover `available` ou usar apenas esse campo

#### Problema 10.2: BigDecimal sem validação
**Tipo**: Data Validation
**Linhas**: 21-22
**Código**:
```java
private BigDecimal priceRent;
private BigDecimal priceSale;
```
**Problema**: 
- Sem validação se são positivos
- Sem scale/precision definidos no banco
- Podem ser null
**Impacto**: MÉDIO
**Solução**:
```java
@DecimalMin("0.00")
@Digits(integer=10, fraction=2)
@NotNull
private BigDecimal priceRent;
```

#### Problema 10.3: Shop pode ser null
**Tipo**: Null Pointer Risk
**Linhas**: 33
**Código**:
```java
@ManyToOne
@JoinColumn(name = "shop_id")
private Shop shop;
```
**Problema**: Sem `@NotNull`, shop pode ser null
**Impacto**: MÉDIO
**Solução**:
```java
@NotNull
@ManyToOne
private Shop shop;
```

---

### 11. **Booking.java** 🟠

**Localização**: `/src/main/java/com/magiclook/data/Booking.java`

#### Problema 11.1: Datas podem ser null
**Tipo**: Null Pointer Risk
**Linhas**: 17-26
**Código**:
```java
private Date pickupDate;
private Date startUseDate;
private Date endUseDate;
private Date returnDate;
```
**Problema**: Sem validação, podem ser null
**Impacto**: MÉDIO
**Solução**:
```java
@NotNull
private Date startUseDate;

@NotNull
private Date endUseDate;
```

#### Problema 11.2: Sem validação de lógica (startDate < endDate)
**Tipo**: Business Logic Validation
**Linhas**: Toda a classe
**Problema**: Sem método que valida se startDate < endDate
**Impacto**: MÉDIO
**Solução**:
```java
@AssertTrue(message = "Data de início deve ser antes da data de fim")
private boolean isValidDateRange() {
    return startUseDate != null && endUseDate != null && 
           startUseDate.before(endUseDate);
}
```

#### Problema 11.3: totalPrice pode ser null ou negativo
**Tipo**: Data Validation
**Linha**: 27
**Código**:
```java
private BigDecimal totalPrice;
```
**Problema**: Sem validação
**Impacto**: MÉDIO
**Solução**:
```java
@NotNull
@DecimalMin("0.00")
private BigDecimal totalPrice;
```

---

### 12. **ItemSingle.java** 🟠

**Localização**: `/src/main/java/com/magiclook/data/ItemSingle.java`

#### Problema 12.1: State como String (enum seria melhor)
**Tipo**: Code Smell - Magic Strings
**Linhas**: 18, 21
**Código**:
```java
public static final String STATE_AVAILABLE = "AVAILABLE";
public static final String STATE_MAINTENANCE = "MAINTENANCE";
private String state;
```
**Problema**: Estados como String é propenso a erros
**Impacto**: MÉDIO
**Solução**:
```java
public enum ItemState {
    AVAILABLE, MAINTENANCE, DAMAGED
}
private ItemState state;
```

#### Problema 12.2: Size como String sem validação
**Tipo**: Data Validation
**Linhas**: 24
**Código**:
```java
private String size;
```
**Problema**: Sem validação de valores válidos (XS, S, M, L, XL)
**Impacto**: MÉDIO
**Solução**:
```java
@Pattern(regexp = "^(XS|S|M|L|XL)$")
private String size;
```

---

## PROBLEMAS TRANSVERSAIS (Em vários arquivos)

### 🔴 1. SEGURANÇA - Senhas em Plaintext
**Arquivos afetados**: UserService, StaffService, DatabaseLoader, User, Staff
**Impacto**: CRÍTICO
**Solução**: Implementar BCrypt/Spring Security em todos os locais

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### 🔴 2. System.out.println / System.err.println
**Arquivos afetados**: ItemService (linha 98)
**Impacto**: ALTO
**Solução**: Remover e usar Logger
```java
private static final Logger logger = LoggerFactory.getLogger(ItemService.class);
logger.error("Erro ao buscar item com ID " + itemId, e);
```

### 🟠 3. Try-catch Exception genérica
**Arquivos afetados**: StaffController, BookingController, ItemService
**Impacto**: ALTO
**Solução**: Capturar exceções específicas e logar

### 🟠 4. Métodos retornam null ao invés de Optional
**Arquivos afetados**: UserService, ItemService
**Impacto**: MÉDIO
**Solução**: Devolver `Optional<T>` ou coleção vazia

### 🟠 5. Hardcoded Strings e Magic Numbers
**Arquivos afetados**: Todos os controllers
**Impacto**: MÉDIO
**Solução**: Usar constantes (já parcialmente feito)

### 🟠 6. Sem logging de operações críticas
**Arquivos afetados**: Todos os services
**Impacto**: BAIXO (Auditoria)
**Solução**: Adicionar Logger

### 🟠 7. Métodos muito longos (> 50 linhas)
**Arquivos afetados**: 
- StaffController.addItem (60 linhas)
- StaffService.updateItem (73 linhas)
- DatabaseLoader.initDatabase (240 linhas)
- BookingService.createBooking (80 linhas)

**Impacto**: MÉDIO
**Solução**: Refatorar em métodos menores

### 🟠 8. Falta de validação de entrada
**Arquivos afetados**: Todos os controllers/services
**Impacto**: MÉDIO
**Solução**: Adicionar validação com @Valid, @NotNull, @Pattern, etc.

### 🟠 9. Sem tratamento de timezone
**Arquivos afetados**: BookingController, BookingService
**Impacto**: BAIXO (Pode causar bugs)
**Solução**: Usar ZonedDateTime ao invés de Date

### 🟠 10. Concorrência - Lock global inadequado
**Arquivos afetados**: BookingService
**Impacto**: ALTO
**Solução**: Usar @Transactional(isolation = SERIALIZABLE)

---

## RESUMO POR SEVERIDADE

### 🔴 CRÍTICA (8 problemas)
1. UserService - Senhas plaintext no registro
2. UserService - Login com plaintext (timing attack)
3. StaffService - Senhas plaintext no login
4. DatabaseLoader - Senhas hardcoded
5. StaffService - IOException não tratada
6. BookingService - Race condition
7. StaffController - NullPointerException (getShop())
8. DatabaseLoader - NPE em shop1/shop2

### 🟠 ALTA (15 problemas)
1. UserService - Retorna null em login
2. UserService - Falta validação
3. StaffController - Multiple setAttribute sem validação
4. StaffController - Exception catching inadequado
5. BookingController - Multiple Exception catching genéricos
6. BookingService - Exceções genéricas
7. ItemService - System.err.println
8. ItemService - Retorna null
9. StaffService - Validação insuficiente de arquivo
10. + mais 5

### 🟡 MÉDIA (22 problemas)
Métodos longos, code smells, validação inadequada, falta de logging, etc.

---

## PLANO DE AÇÃO RECOMENDADO

### Fase 1 (URGENTE - Segurança)
- [ ] Implementar BCrypt para senhas
- [ ] Remover senhas hardcoded
- [ ] Implementar Spring Security
- [ ] Validar entrada em todos os endpoints

### Fase 2 (IMPORTANTE - Estabilidade)
- [ ] Resolver NullPointerExceptions
- [ ] Tratamento adequado de exceções
- [ ] Adicionar validações em entidades (JSR-380)
- [ ] Remover System.out/err

### Fase 3 (MELHORIA - Qualidade)
- [ ] Refatorar métodos longos
- [ ] Adicionar logging
- [ ] Usar LocalDateTime ao invés de Date
- [ ] Usar Optional ao invés de null
- [ ] Usar enums para states ao invés de strings

### Fase 4 (MANUTENIBILIDADE)
- [ ] Adicionar testes unitários
- [ ] Documentação de exceções
- [ ] Code review com foco em padrões

---

## CONFIGURAÇÃO SONARQUBE RECOMENDADA

```properties
# sonar-project.properties
sonar.projectKey=MagicLook
sonar.projectName=MagicLook
sonar.projectVersion=1.0
sonar.java.binaries=target/classes
sonar.coverage.exclusions=**/*DTO.java,**/*Entity.java
sonar.java.checkstyle.reportPaths=target/checkstyle-result.xml

# Regras importantes
sonar.issue.ignore.multicriteria=e1,e2,e3
sonar.issue.ignore.multicriteria.e1.ruleKey=java:S1192
sonar.issue.ignore.multicriteria.e1.resourceKey=**/*DTO.java
```

---

## CONCLUSÃO

O código tem **boas práticas em algumas áreas** (uso de constantes, estrutura de controllers), mas **problemas críticos de segurança** (senhas plaintext) e **deficiências em tratamento de erros e validação**.

Recomenda-se priorizar a segurança antes de qualquer outra coisa, pois o sistema está expostos a riscos graves.

**Esforço estimado de correção**: 40-60 horas de desenvolvimento.

---

*Análise realizada em 17 de Janeiro de 2026*
