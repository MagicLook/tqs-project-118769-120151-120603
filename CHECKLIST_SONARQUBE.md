# Checklist Executivo - Correções SonarQube

## 📊 RESUMO GERAL

- **Total de Problemas**: 45+
- **Críticos**: 8 🔴
- **Altos**: 15 🟠  
- **Médios**: 22 🟡
- **Arquivos Afetados**: 12
- **Esforço Estimado**: 40-60 horas

---

## 🔴 PROBLEMAS CRÍTICOS (Fazer AGORA)

### [ ] 1. Segurança - Senhas em Plaintext
- [ ] Criar `SecurityConfig.java` com BCrypt
- [ ] Atualizar `UserService.login()` e `register()`
- [ ] Atualizar `StaffService.login()`
- [ ] Remover senhas hardcoded de `DatabaseLoader.java`
- [ ] Adicionar `@Autowired PasswordEncoder` em todos os services

**Tempo**: 4-6 horas

### [ ] 2. NullPointerException - staff.getShop()
- [ ] Adicionar null check em `StaffController.staffLogin()` linha 56
- [ ] Validar que Staff sempre tem Shop

**Tempo**: 1 hora

### [ ] 3. IOException não tratada em StaffService
- [ ] Wrapp `image.getInputStream()` em try-with-resources
- [ ] Adicionar logging apropriado
- [ ] Adicionar validação de arquivo

**Tempo**: 2 horas

### [ ] 4. Race Condition em BookingService
- [ ] Remover `GLOBAL_BOOKING_LOCK`
- [ ] Adicionar `@Transactional(isolation = Isolation.SERIALIZABLE)`
- [ ] Testar sob concorrência

**Tempo**: 3 horas

### [ ] 5. DatabaseLoader - NPE em shop1/shop2
- [ ] Adicionar validação que shops foram criadas
- [ ] Refatorar em métodos menores
- [ ] Adicionar exception handling

**Tempo**: 2-3 horas

---

## 🟠 PROBLEMAS ALTOS (Fazer esta semana)

### [ ] 6. Exception Handling Inadequado
Arquivos afetados:
- [ ] StaffController (3 catch blocks)
- [ ] BookingController (6 catch blocks)
- [ ] ItemService (1 catch block)

**Tarefas**:
- [ ] Remover `System.err.println` (ItemService:98)
- [ ] Capturar exceções específicas ao invés de Exception genérica
- [ ] Adicionar logging com `logger.error()`

**Tempo**: 5-6 horas

### [ ] 7. Retornar Optional ao invés de null
Arquivos:
- [ ] `UserService.login()` - retorna `null`
- [ ] `ItemService.getItemById()` - retorna `null`

**Tarefas**:
- [ ] Converter para `Optional<T>`
- [ ] Atualizar controllers para usar `.orElse()` ou `.orElseThrow()`

**Tempo**: 3 horas

### [ ] 8. Refatorar Métodos Muito Longos
- [ ] DatabaseLoader.initDatabase() (240 linhas) → 5 métodos
- [ ] StaffService.updateItem() (73 linhas) → 3 métodos
- [ ] BookingService.createBooking() (80 linhas) → 4 métodos
- [ ] StaffController.addItem() (60 linhas) → 5 métodos

**Tempo**: 8-10 horas

### [ ] 9. Validação de Entrada Inadequada
- [ ] Adicionar `@Valid` nas anotações de parâmetros
- [ ] Adicionar validação de `@Email`, `@NotBlank`, `@Pattern`
- [ ] Adicionar `@DecimalMin` para preços

**Tempo**: 4-5 horas

### [ ] 10. Criar Custom Exceptions
- [ ] `ItemNotFoundException`
- [ ] `ItemNotAvailableException`
- [ ] `InvalidBookingException`

**Tempo**: 1-2 horas

---

## 🟡 PROBLEMAS MÉDIOS (Fazer próximas 2 semanas)

### [ ] 11. Adicionar Logging
Todos os services e controllers:
- [ ] Adicionar `Logger logger = LoggerFactory.getLogger(...)`
- [ ] Log em operações críticas (login, criar reserva, adicionar item)
- [ ] Log de erros com stack trace

**Tempo**: 6-8 horas

### [ ] 12. Validações em Entidades
- [ ] `User.java` - @Email, @NotBlank, @Pattern
- [ ] `Item.java` - @DecimalMin, @NotNull, @Digits
- [ ] `Booking.java` - @NotNull, @DecimalMin, @AssertTrue
- [ ] `ItemSingle.java` - @Pattern para Size e State

**Tempo**: 3-4 horas

### [ ] 13. Usar LocalDateTime ao invés de Date
- [ ] Converter `Date` para `LocalDateTime` em BookingService
- [ ] Remover `Calendar.getInstance()`
- [ ] Usar `ZonedDateTime` para timezone

**Tempo**: 5-6 horas

### [ ] 14. Code Smells - Magic Strings
- [ ] Criar constantes para estados (ItemSingle.STATE_*)
- [ ] Criar enum para Size (XS, S, M, L, XL)
- [ ] Criar enum para Gender (M, F)

**Tempo**: 3 horas

### [ ] 15. Harmonia entre Convenções
- [ ] Remover métodos de teste direto em controllers
- [ ] Padronizar naming de atributos de sessão
- [ ] Usar `Constants` class para todas as strings

**Tempo**: 2-3 horas

---

## 📋 CHECKLIST DETALHADO POR ARQUIVO

### UserService.java (6 itens)
- [ ] Importar PasswordEncoder
- [ ] Usar passwordEncoder.encode() no register
- [ ] Usar passwordEncoder.matches() no login
- [ ] Retornar Optional em login
- [ ] Adicionar validações de entrada
- [ ] Adicionar logging

### StaffService.java (5 itens)
- [ ] Usar PasswordEncoder.matches() no login
- [ ] Tratar IOException em saveImage com try-with-resources
- [ ] Validar tamanho/tipo de arquivo
- [ ] Refatorar updateItem() em 3 métodos
- [ ] Adicionar logging

### UserController.java (4 itens)
- [ ] Usar Optional de UserService.login()
- [ ] Adicionar mais constantes para URLs
- [ ] Adicionar logging para operações críticas
- [ ] Validar inputs com @Valid

### StaffController.java (5 itens)
- [ ] Null check para staff.getShop()
- [ ] Refatorar exception handling (3 catches)
- [ ] Refatorar addItem em métodos menores
- [ ] Melhorar erro messages em DeleteMapping
- [ ] Adicionar logging

### BookingController.java (6 itens)
- [ ] Refatorar exception handling (6 catches)
- [ ] UUID.fromString() com try-catch
- [ ] Adicionar logging em operações críticas
- [ ] Melhorar tratamento de null em bookings
- [ ] Considerar usar LocalDate ao invés de Date
- [ ] Remover métodos de teste duplicados

### BookingService.java (7 itens)
- [ ] Remover GLOBAL_BOOKING_LOCK
- [ ] Adicionar @Transactional(isolation = SERIALIZABLE)
- [ ] Criar custom exceptions (ItemNotAvailableException, etc)
- [ ] Refatorar createBooking() em 4 métodos
- [ ] Remover Calendar, usar LocalDateTime
- [ ] Adicionar logging
- [ ] Adicionar validações

### ItemService.java (5 itens)
- [ ] Remover System.err.println (linha 98)
- [ ] Adicionar Logger
- [ ] Retornar Optional em getItemById()
- [ ] Validar gender parameter
- [ ] Adicionar try-catch específicos

### DatabaseLoader.java (6 itens)
- [ ] Refatorar em 5 métodos (initShops, initUsers, initStaff, initItemTypes, initItems)
- [ ] Remover senhas hardcoded
- [ ] Adicionar validação de NPE para shop1/shop2
- [ ] Usar PasswordEncoder.encode()
- [ ] Adicionar logging
- [ ] Adicionar exception handling

### User.java (4 itens)
- [ ] Adicionar @Email para email
- [ ] Adicionar @NotBlank para username, firstName, lastName
- [ ] Adicionar @Pattern para telephone
- [ ] Adicionar @Length para password

### Item.java (5 itens)
- [ ] Remover campo `available` (redundante)
- [ ] Adicionar @NotNull para shop
- [ ] Adicionar @DecimalMin para priceRent e priceSale
- [ ] Adicionar @Digits para BigDecimal
- [ ] Adicionar @NotBlank para name

### Booking.java (5 itens)
- [ ] Adicionar @NotNull para startUseDate, endUseDate
- [ ] Adicionar @NotNull para totalPrice
- [ ] Adicionar @DecimalMin para totalPrice
- [ ] Adicionar @AssertTrue para validar datas
- [ ] Considerar usar LocalDateTime ao invés de Date

### ItemSingle.java (3 itens)
- [ ] Converter state para Enum ao invés de String
- [ ] Adicionar @Pattern para size (XS|S|M|L|XL)
- [ ] Adicionar @NotBlank para size

---

## 🎯 FASES DE IMPLEMENTAÇÃO

### FASE 1: SEGURANÇA (Semana 1 - 10 horas)
**Objetivo**: Eliminar vulnerabilidades críticas

1. [ ] Criar SecurityConfig com BCrypt
2. [ ] Atualizar UserService e StaffService
3. [ ] Atualizar DatabaseLoader
4. [ ] Testar login/register com senhas encriptadas

**Prioridade**: 🔴 CRÍTICA

### FASE 2: ESTABILIDADE (Semana 2 - 15 horas)
**Objetivo**: Evitar crashes e exceções

1. [ ] Resolver NullPointerExceptions
2. [ ] Implementar proper exception handling
3. [ ] Remover System.out/err
4. [ ] Usar Optional ao invés de null

**Prioridade**: 🟠 ALTA

### FASE 3: REFATORAÇÃO (Semana 3-4 - 20 horas)
**Objetivo**: Melhorar qualidade e manutenibilidade

1. [ ] Refatorar métodos longos
2. [ ] Adicionar validações em entidades
3. [ ] Adicionar logging
4. [ ] Criar custom exceptions

**Prioridade**: 🟡 MÉDIA

### FASE 4: TESTES (Semana 5 - 15 horas)
**Objetivo**: Garantir que tudo funciona

1. [ ] Testes unitários para services
2. [ ] Testes de integração para controllers
3. [ ] Testes de segurança (password encoding)
4. [ ] Testes de concorrência para BookingService

**Prioridade**: 🟡 MÉDIA

---

## 📈 MÉTRICAS ESPERADAS APÓS CORREÇÕES

| Métrica | Antes | Depois |
|---------|-------|--------|
| Bugs | ~45 | ~5 |
| Code Smells | ~35 | ~5 |
| Vulnerabilidades | 8 CRÍTICAS | 0 |
| Duplicação | ~8% | ~2% |
| Cobertura de testes | ~30% | ~70% |
| Complexidade ciclomática | Média: 12 | Média: 6 |

---

## 🚀 PRÓXIMOS PASSOS

1. **Hoje**: Revisar este documento
2. **Amanhã**: Começar Fase 1 (Segurança)
3. **Próxima semana**: Completar Fase 1 e 2
4. **Próximas 2 semanas**: Fases 3 e 4

---

## 📞 CONTATO PARA DÚVIDAS

Consulte os arquivos:
- `ANALISE_SONARQUBE.md` - Análise detalhada
- `CORRECOES_SONARQUBE.md` - Código pronto para implementar
- Este documento - Checklist e plano de ação

---

**Última atualização**: 17 de Janeiro de 2026
**Criado para**: Projeto MagicLook
**Status**: Pronto para implementação
