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
                         ItemRepository itemRepository, ItemSingleRepository itemSingleRepository, BookingRepository bookingRepository) {
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
            User user1 = new User("Maria", "Silva", "maria@gmail.com", "911991911", passwordEncoder.encode("maria?"), "maria"); //NOSONAR
            User user2 = new User("Gonçalo", "Floros", "goncalo@gmail.com", "911991912", passwordEncoder.encode("goncalo?"), "goncalo"); //NOSONAR
            User user3 = new User("Pedro", "Silva", "pedro@gmail.com", "911991913", passwordEncoder.encode("pedro?"), "pedro"); //NOSONAR

            userRepository.save(user1);
            userRepository.save(user2);
            userRepository.save(user3);

            userRepository.flush();
            logger.info("Sample users initialized with hashed passwords");
        }

        // Init staff
        if (staffRepository.count() == 0) {
            Staff staff1 = new Staff("Admin", "admin@gmail.com", passwordEncoder.encode("admin123"), "admin", shop1); //NOSONAR
            Staff staff2 = new Staff("Admin2", "admin2@gmail.com", passwordEncoder.encode("admin123"), "admin2", shop2); //NOSONAR

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
            Item item1 = new Item(JUMPSUIT + " Valentina", POLYESTER, BLUE, BEAUTY_BRAND, 
                    new BigDecimal(95), new BigDecimal(950), shop1, itemType4);
            item1.setImagePath(uploadDir + "/Macacao1.jpeg");
            itemRepository.save(item1);

            // Adicionar instâncias
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item1, S));
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item1, M));
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item1, L));

            Item item2 = new Item(JUMPSUIT + " Riveira", POLYESTER, BLACK, BEAUTY_BRAND, 
                    new BigDecimal(90), new BigDecimal(900), shop1, itemType5);
            item2.setImagePath(uploadDir + "/Macacao2.webp");
            itemRepository.save(item2);

            // Adicionar instâncias
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item2, XS));
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item2, S));
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item2, M));
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item2, L));

            Item item3 = new Item(JUMPSUIT + " Madeline", POLYESTER, BEIGE, BEAUTY_BRAND, 
                    new BigDecimal(120), new BigDecimal(1200), shop1, itemType6);
            item3.setImagePath(uploadDir + "/Macacao3.jpg");
            itemRepository.save(item3);

            // Adicionar instâncias
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item3, XS));
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item3, S));
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item3, M));

            // Fatos
            Item item5 = new Item(SUIT + " Oxford", POLYESTER, BLACK, NICOLA, 
                    new BigDecimal(90), new BigDecimal(900), shop1, itemType7);
            item5.setImagePath(uploadDir + "/Fato1.jpg");
            itemRepository.save(item5);

            // Adicionar instâncias
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item5, XS));
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item5, S));
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item5, M));
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item5, L));
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item5, XL));

            Item item6 = new Item(SUIT + " Cartier", POLYESTER, BLACK, PAULINE, 
                    new BigDecimal(110), new BigDecimal(1100), shop2, itemType8);
            item6.setImagePath(uploadDir + "/Fato2.jpg");
            itemRepository.save(item6);

            // Adicionar instâncias
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item6, S));
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item6, M));
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item6, L));

            Item item7 = new Item(SUIT + " Monaco", POLYESTER, BLUE, PIZHON, 
                    new BigDecimal(100), new BigDecimal(1000), shop1, itemType8);
            item7.setImagePath(uploadDir + "/Fato3.webp");
            itemRepository.save(item7);

            // Adicionar instâncias
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item7, S));
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item7, M));
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item7, L));
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item7, XL));

            // Vestidos
            Item item8 = new Item(DRESS + " Lola", POLYESTER, BLUE, OKSANA_MUKHA, 
                    new BigDecimal(100), new BigDecimal(1000), shop1, itemType1);
            item8.setImagePath(uploadDir + "/Vestido1.jpg");
            itemRepository.save(item8);

            // Adicionar instâncias
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item8, S));
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item8, M));

            Item item9 = new Item(DRESS + " Carmen", POLYESTER, RED, OKSANA_MUKHA, 
                    new BigDecimal(150), new BigDecimal(1500), shop1, itemType1);
            item9.setImagePath(uploadDir + "/Vestido2.jpg");
            itemRepository.save(item9);

            // Adicinar instâncias
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item9, XS));
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item9, S));
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item9, M));

            Item item10 = new Item(DRESS + " Peach", POLYESTER, PINK, OKSANA_MUKHA, 
                    new BigDecimal(180), new BigDecimal(1800), shop1, itemType2);
            item10.setImagePath(uploadDir + "/Vestido3.jpg");
            itemRepository.save(item10);

            // Adicionar instâncias
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item10, S));
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item10, M));
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item10, L));
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item10, XL));

            Item item11 = new Item(DRESS + " Gaia", POLYESTER, BLACK, OKSANA_MUKHA, 
                    new BigDecimal(200), new BigDecimal(2000), shop2, itemType2);
            item11.setImagePath(uploadDir + "/Vestido4.jpg");
            itemRepository.save(item11);

            // Adicionar instâncias
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item11, XS));
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item11, S));
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item11, M));

            Item item12 = new Item(DRESS + " Monet", POLYESTER, BLACK, OKSANA_MUKHA, 
                    new BigDecimal(230), new BigDecimal(2300), shop2, itemType3);
            item12.setImagePath(uploadDir + "/Vestido5.jpg");
            itemRepository.save(item12);

            // Adicionar instâncias
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item12, M));
            itemSingleRepository.save(new ItemSingle(AVAILABLE, item12, L));

            Item item13 = new Item(DRESS + " Sky", POLYESTER, BLUE, OKSANA_MUKHA, 
                    new BigDecimal(250), new BigDecimal(2500), shop1, itemType3);
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
                        userRepository.findByUsername("maria")
            );

            booking1.setTotalDays(4);
            booking1.setTotalPrice(item1.getPriceRent().multiply(new BigDecimal(4)));

            bookingRepository.save(booking1);
        }
    }
}