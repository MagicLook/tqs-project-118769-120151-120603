package com.magiclook.loader;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.magiclook.repository.*;
import com.magiclook.data.*;

@Component
public class DatabaseLoader {

        private static final Logger logger = LoggerFactory.getLogger(DatabaseLoader.class);
        private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        // Constants for shops
        private static final String LOCATION1 = "Porto";
        private static final String LOCATION2 = "Lisboa";

        // Constants for item types
        private static final String DRESS = "Vestido";
        private static final String SHORT = "Curto";
        private static final String MEDIUM = "Médio";
        private static final String LONG = "Comprido";
        private static final String JUMPSUIT = "Macacão";
        private static final String SUIT = "Fato";
        private static final String SIMPLE = "Simples";
        private static final String THREE_PIECE = "Três peças";

        // Constants for item attributes
        private static final String BEAUTY_BRAND = "Beauty";
        private static final String POLYESTER = "Poliéster";
        private static final String AVAILABLE = "AVAILABLE";
        private static final String BLACK = "Preto";
        private static final String OKSANA_MUKHA = "Oksana Mukha";

        // Constants for colors
        private static final String BLUE = "Azul";
        private static final String BEIGE = "Bege";
        private static final String RED = "Vermelho";
        private static final String PINK = "Rosa";

        // Constants for other brands
        private static final String NICOLA = "NICOLA";
        private static final String PAULINE = "PAULINE";
        private static final String PIZHON = "PIZHON";

        // Constants for sizes
        private static final String XS = "XS";
        private static final String S = "S";
        private static final String M = "M";
        private static final String L = "L";
        private static final String XL = "XL";

        private final ShopRepository shopRepository;
        private final UserRepository userRepository;
        private final StaffRepository staffRepository;
        private final ItemTypeRepository itemTypeRepository;
        private final ItemRepository itemRepository;
        private final ItemSingleRepository itemSingleRepository;
        private final BookingRepository bookingRepository;

        @Value("${app.upload.dir}")
        private String uploadDir;

        public DatabaseLoader(ShopRepository shopRepository, UserRepository userRepository,
                        StaffRepository staffRepository, ItemTypeRepository itemTypeRepository,
                        ItemRepository itemRepository, ItemSingleRepository itemSingleRepository,
                        BookingRepository bookingRepository) {
                this.shopRepository = shopRepository;
                this.userRepository = userRepository;
                this.staffRepository = staffRepository;
                this.itemTypeRepository = itemTypeRepository;
                this.itemRepository = itemRepository;
                this.itemSingleRepository = itemSingleRepository;
                this.bookingRepository = bookingRepository;
        }

        @PostConstruct
        @SuppressWarnings("java:S2115") // Hardcoded passwords are intentional for test data initialization
        void initDatabase() {
                // Init shops
                Shop shop1;
                Shop shop2;

                if (shopRepository.count() == 0) {
                        shop1 = new Shop(LOCATION1, LOCATION1);
                        shop2 = new Shop(LOCATION2, LOCATION2);

                        shopRepository.save(shop1);
                        shopRepository.save(shop2);

                        shopRepository.flush();
                        logger.info("Sample shops initialized");
                } else {
                        shop1 = shopRepository.findByNameAndLocation(LOCATION1, LOCATION1).orElse(null);
                        shop2 = shopRepository.findByNameAndLocation(LOCATION2, LOCATION2).orElse(null);

                        if (shop1 == null || shop2 == null) {
                                logger.error("Failed to load required shops from database");
                                throw new IllegalStateException("Required shops not found in database");
                        }
                }

                // Init users
                if (userRepository.count() == 0) {
                        User user1 = new User("Maria", "Silva", "maria@gmail.com", "911991911",
                                        passwordEncoder.encode("maria?"), "maria"); // NOSONAR
                        User user2 = new User("Gonçalo", "Floros", "goncalo@gmail.com", "911991912",
                                        passwordEncoder.encode("goncalo?"), "goncalo"); // NOSONAR
                        User user3 = new User("Pedro", "Silva", "pedro@gmail.com", "911991913",
                                        passwordEncoder.encode("pedro?"), "pedro"); // NOSONAR

                        userRepository.save(user1);
                        userRepository.save(user2);
                        userRepository.save(user3);

                        userRepository.flush();
                        logger.info("Sample users initialized with hashed passwords");
                }

                // Init staff
                if (staffRepository.count() == 0) {
                        Staff staff1 = new Staff("Admin", "admin@gmail.com", passwordEncoder.encode("admin123"),
                                        "admin", shop1); // NOSONAR
                        Staff staff2 = new Staff("Admin2", "admin2@gmail.com", passwordEncoder.encode("admin123"),
                                        "admin2", shop2); // NOSONAR

                        staffRepository.save(staff1);
                        staffRepository.save(staff2);

                        staffRepository.flush();
                        logger.info("Sample staff initialized with hashed passwords");
                }

                // Init ItemTypes
                ItemType itemType1;
                ItemType itemType2;
                ItemType itemType3;
                ItemType itemType4;
                ItemType itemType5;
                ItemType itemType6;
                ItemType itemType7;
                ItemType itemType8;

                if (itemTypeRepository.count() == 0) {
                        itemType1 = new ItemType("F", DRESS, SHORT);
                        itemType2 = new ItemType("F", DRESS, MEDIUM);
                        itemType3 = new ItemType("F", DRESS, LONG);
                        itemType4 = new ItemType("F", JUMPSUIT, SHORT);
                        itemType5 = new ItemType("F", JUMPSUIT, MEDIUM);
                        itemType6 = new ItemType("F", JUMPSUIT, LONG);
                        itemType7 = new ItemType("M", SUIT, SIMPLE);
                        itemType8 = new ItemType("M", SUIT, THREE_PIECE);

                        itemTypeRepository.save(itemType1);
                        itemTypeRepository.save(itemType2);
                        itemTypeRepository.save(itemType3);
                        itemTypeRepository.save(itemType4);
                        itemTypeRepository.save(itemType5);
                        itemTypeRepository.save(itemType6);
                        itemTypeRepository.save(itemType7);
                        itemTypeRepository.save(itemType8);

                        itemTypeRepository.flush();
                } else {
                        itemType1 = itemTypeRepository.findByGenderAndCategoryAndSubcategory("F", DRESS, SHORT);
                        itemType2 = itemTypeRepository.findByGenderAndCategoryAndSubcategory("F", DRESS, MEDIUM);
                        itemType3 = itemTypeRepository.findByGenderAndCategoryAndSubcategory("F", DRESS, LONG);
                        itemType4 = itemTypeRepository.findByGenderAndCategoryAndSubcategory("F", JUMPSUIT, SHORT);
                        itemType5 = itemTypeRepository.findByGenderAndCategoryAndSubcategory("F", JUMPSUIT, MEDIUM);
                        itemType6 = itemTypeRepository.findByGenderAndCategoryAndSubcategory("F", JUMPSUIT, LONG);
                        itemType7 = itemTypeRepository.findByGenderAndCategoryAndSubcategory("M", SUIT, SIMPLE);
                        itemType8 = itemTypeRepository.findByGenderAndCategoryAndSubcategory("M", SUIT, THREE_PIECE);
                }

                // Init Items
                if (itemRepository.count() == 0) {
                        // Macacões
                        Item item1 = Item.builder()
                                        .name(JUMPSUIT + " Valentina")
                                        .material(POLYESTER)
                                        .color(BLUE)
                                        .brand(BEAUTY_BRAND)
                                        .priceRent(new BigDecimal(95))
                                        .priceSale(new BigDecimal(950))
                                        .shop(shop1)
                                        .itemType(itemType4)
                                        .build();
                        item1.setImagePath(uploadDir + "/Macacao1.jpeg");
                        itemRepository.save(item1);

                        // Adicionar instâncias
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item1, S));
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item1, M));
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item1, L));

                        Item item2 = Item.builder()
                                        .name(JUMPSUIT + " Riveira")
                                        .material(POLYESTER)
                                        .color(BLACK)
                                        .brand(BEAUTY_BRAND)
                                        .priceRent(new BigDecimal(90))
                                        .priceSale(new BigDecimal(900))
                                        .shop(shop1)
                                        .itemType(itemType5)
                                        .build();
                        item2.setImagePath(uploadDir + "/Macacao2.webp");
                        itemRepository.save(item2);

                        // Adicionar instâncias
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item2, XS));
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item2, S));
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item2, M));
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item2, L));

                        Item item3 = Item.builder()
                                        .name(JUMPSUIT + " Madeline")
                                        .material(POLYESTER)
                                        .color(BEIGE)
                                        .brand(BEAUTY_BRAND)
                                        .priceRent(new BigDecimal(120))
                                        .priceSale(new BigDecimal(1200))
                                        .shop(shop1)
                                        .itemType(itemType6)
                                        .build();
                        item3.setImagePath(uploadDir + "/Macacao3.jpg");
                        itemRepository.save(item3);

                        // Adicionar instâncias
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item3, XS));
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item3, S));
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item3, M));

                        // Fatos
                        Item item5 = Item.builder()
                                        .name(SUIT + " Oxford")
                                        .material(POLYESTER)
                                        .color(BLACK)
                                        .brand(NICOLA)
                                        .priceRent(new BigDecimal(90))
                                        .priceSale(new BigDecimal(900))
                                        .shop(shop1)
                                        .itemType(itemType7)
                                        .build();
                        item5.setImagePath(uploadDir + "/Fato1.jpg");
                        itemRepository.save(item5);

                        // Adicionar instâncias
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item5, XS));
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item5, S));
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item5, M));
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item5, L));
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item5, XL));

                        Item item6 = Item.builder()
                                        .name(SUIT + " Cartier")
                                        .material(POLYESTER)
                                        .color(BLACK)
                                        .brand(PAULINE)
                                        .priceRent(new BigDecimal(110))
                                        .priceSale(new BigDecimal(1100))
                                        .shop(shop2)
                                        .itemType(itemType8)
                                        .build();
                        item6.setImagePath(uploadDir + "/Fato2.jpg");
                        itemRepository.save(item6);

                        // Adicionar instâncias
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item6, S));
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item6, M));
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item6, L));

                        Item item7 = Item.builder()
                                        .name(SUIT + " Monaco")
                                        .material(POLYESTER)
                                        .color(BLUE)
                                        .brand(PIZHON)
                                        .priceRent(new BigDecimal(100))
                                        .priceSale(new BigDecimal(1000))
                                        .shop(shop1)
                                        .itemType(itemType8)
                                        .build();
                        item7.setImagePath(uploadDir + "/Fato3.webp");
                        itemRepository.save(item7);

                        // Adicionar instâncias
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item7, S));
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item7, M));
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item7, L));
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item7, XL));

                        // Vestidos
                        Item item8 = Item.builder()
                                        .name(DRESS + " Lola")
                                        .material(POLYESTER)
                                        .color(BLUE)
                                        .brand(OKSANA_MUKHA)
                                        .priceRent(new BigDecimal(100))
                                        .priceSale(new BigDecimal(1000))
                                        .shop(shop1)
                                        .itemType(itemType1)
                                        .build();
                        item8.setImagePath(uploadDir + "/Vestido1.jpg");
                        itemRepository.save(item8);

                        // Adicionar instâncias
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item8, S));
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item8, M));

                        Item item9 = Item.builder()
                                        .name(DRESS + " Carmen")
                                        .material(POLYESTER)
                                        .color(RED)
                                        .brand(OKSANA_MUKHA)
                                        .priceRent(new BigDecimal(150))
                                        .priceSale(new BigDecimal(1500))
                                        .shop(shop1)
                                        .itemType(itemType1)
                                        .build();
                        item9.setImagePath(uploadDir + "/Vestido2.jpg");
                        itemRepository.save(item9);

                        // Adicinar instâncias
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item9, XS));
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item9, S));
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item9, M));

                        Item item10 = Item.builder()
                                        .name(DRESS + " Peach")
                                        .material(POLYESTER)
                                        .color(PINK)
                                        .brand(OKSANA_MUKHA)
                                        .priceRent(new BigDecimal(180))
                                        .priceSale(new BigDecimal(1800))
                                        .shop(shop1)
                                        .itemType(itemType2)
                                        .build();
                        item10.setImagePath(uploadDir + "/Vestido3.jpg");
                        itemRepository.save(item10);

                        // Adicionar instâncias
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item10, S));
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item10, M));
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item10, L));
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item10, XL));

                        Item item11 = Item.builder()
                                        .name(DRESS + " Gaia")
                                        .material(POLYESTER)
                                        .color(BLACK)
                                        .brand(OKSANA_MUKHA)
                                        .priceRent(new BigDecimal(200))
                                        .priceSale(new BigDecimal(2000))
                                        .shop(shop2)
                                        .itemType(itemType2)
                                        .build();
                        item11.setImagePath(uploadDir + "/Vestido4.jpg");
                        itemRepository.save(item11);

                        // Adicionar instâncias
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item11, XS));
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item11, S));
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item11, M));

                        Item item12 = Item.builder()
                                        .name(DRESS + " Monet")
                                        .material(POLYESTER)
                                        .color(BLACK)
                                        .brand(OKSANA_MUKHA)
                                        .priceRent(new BigDecimal(230))
                                        .priceSale(new BigDecimal(2300))
                                        .shop(shop2)
                                        .itemType(itemType3)
                                        .build();
                        item12.setImagePath(uploadDir + "/Vestido5.jpg");
                        itemRepository.save(item12);

                        // Adicionar instâncias
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item12, M));
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item12, L));

                        Item item13 = Item.builder()
                                        .name(DRESS + " Sky")
                                        .material(POLYESTER)
                                        .color(BLUE)
                                        .brand(OKSANA_MUKHA)
                                        .priceRent(new BigDecimal(250))
                                        .priceSale(new BigDecimal(2500))
                                        .shop(shop1)
                                        .itemType(itemType3)
                                        .build();
                        item13.setImagePath(uploadDir + "/Vestido6.jpg");
                        itemRepository.save(item13);

                        // Adicionar instâncias
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item13, S));
                        itemSingleRepository.save(new ItemSingle(AVAILABLE, item13, M));

                        // Adicionar uma reserva para testes
                        java.time.LocalDate day1 = java.time.LocalDate.now().plusMonths(1).withDayOfMonth(1);
                        java.time.LocalDate day2 = day1.plusDays(1);
                        java.time.LocalDate day3 = day1.plusDays(2);
                        java.time.LocalDate day4 = day1.plusDays(3);
                        Booking booking1 = new Booking(
                                        java.sql.Date.valueOf(day1),
                                        java.sql.Date.valueOf(day2),
                                        java.sql.Date.valueOf(day3),
                                        java.sql.Date.valueOf(day4),
                                        "RESERVED",
                                        item1,
                                        userRepository.findByUsername("maria"));

                        booking1.setTotalDays(4);
                        booking1.setTotalPrice(item1.getPriceRent().multiply(new BigDecimal(4)));

                        bookingRepository.save(booking1);
                }
        }
}