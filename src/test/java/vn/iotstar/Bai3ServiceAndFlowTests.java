package vn.iotstar;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.*;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.mapper.ProductMapper;
import vn.iotstar.mapper.UserMapper;
import vn.iotstar.repository.OtpTokenRepository;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.AuthService;
import vn.iotstar.service.OtpService;
import vn.iotstar.service.ProductService;
import vn.iotstar.service.UserService;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class Bai3ServiceAndFlowTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OtpTokenRepository otpTokenRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private AuthService authService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    @Test
    @DisplayName("Verify UserMapper abstract class maps correctly")
    void testUserMapper() {
        Role role = roleRepository.findByName("ROLE_USER").orElseGet(() ->
            roleRepository.save(Role.builder().name("ROLE_USER").build())
        );

        User user = User.builder()
                .id(100L)
                .username("mapper_user")
                .email("mapper@test.com")
                .fullName("Mapper Test")
                .role(role)
                .enabled(true)
                .build();

        UserDTO dto = userMapper.toDTO(user);
        assertNotNull(dto);
        assertEquals("mapper_user", dto.getUsername());
        assertEquals("ROLE_USER", dto.getRoleName());
    }

    @Test
    @DisplayName("Verify ProductMapper abstract class maps correctly")
    void testProductMapper() {
        Role role = roleRepository.findByName("ROLE_USER").orElseGet(() ->
            roleRepository.save(Role.builder().name("ROLE_USER").build())
        );

        User user = userRepository.save(User.builder()
                .username("prod_owner")
                .email("owner@test.com")
                .password("123456")
                .fullName("Owner")
                .role(role)
                .enabled(true)
                .build());

        Product product = Product.builder()
                .id(200L)
                .name("Test Phone")
                .description("Good phone")
                .price(BigDecimal.valueOf(15000000))
                .user(user)
                .imageUrl("https://cloud.com/img.jpg|public_123")
                .build();

        ProductDTO dto = productMapper.toDTO(product);
        assertNotNull(dto);
        assertEquals("Test Phone", dto.getName());
        assertEquals(user.getId(), dto.getUserId());
        assertEquals("prod_owner", dto.getUsername());
    }

    @Test
    @DisplayName("Verify User CRUD, search, pagination, and count")
    void testUserCrudAndCounts() {
        long initialCount = userService.countUsers();

        UserDTO createDto = UserDTO.builder()
                .username("crud_user")
                .email("crud@test.com")
                .fullName("CRUD User")
                .roleName("ROLE_USER")
                .enabled(true)
                .build();

        UserDTO created = userService.create(createDto);
        assertNotNull(created.getId());
        assertEquals(initialCount + 1, userService.countUsers());

        // Search
        Page<UserDTO> searchResult = userService.findAll("crud_user", 0, 10);
        assertTrue(searchResult.getTotalElements() >= 1);

        // Update
        created.setFullName("CRUD User Updated");
        UserDTO updated = userService.update(created.getId(), created);
        assertEquals("CRUD User Updated", updated.getFullName());

        // Delete
        userService.delete(created.getId());
        assertEquals(initialCount, userService.countUsers());
    }

    @Test
    @DisplayName("Verify Product CRUD and count by user")
    void testProductCrudAndCounts() {
        Role role = roleRepository.findByName("ROLE_USER").orElseGet(() ->
            roleRepository.save(Role.builder().name("ROLE_USER").build())
        );

        User user = userRepository.save(User.builder()
                .username("seller_user")
                .email("seller@test.com")
                .password("123456")
                .fullName("Seller User")
                .role(role)
                .enabled(true)
                .build());

        ProductDTO dto = new ProductDTO();
        dto.setName("Laptop Gaming");
        dto.setDescription("High performance");
        dto.setPrice(BigDecimal.valueOf(25000000));
        dto.setUserId(user.getId());

        ProductDTO created = productService.create(dto, null);
        assertNotNull(created.getId());
        assertEquals(1, productService.countByUser(user.getId()));

        // Search
        Page<ProductDTO> searchPage = productService.findAll("Laptop", 0, 10);
        assertTrue(searchPage.getTotalElements() >= 1);

        // Delete
        productService.delete(created.getId());
        assertEquals(0, productService.countByUser(user.getId()));
    }

    @Test
    @DisplayName("Verify Registration -> OTP -> Verify Flow")
    void testRegistrationAndOtpFlow() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("new_registrant");
        dto.setEmail("registrant@test.com");
        dto.setFullName("New Registrant");
        dto.setPassword("123456");
        dto.setConfirmPassword("123456");

        authService.register(dto);

        User user = userRepository.findByEmail("registrant@test.com").orElse(null);
        assertNotNull(user);
        assertFalse(user.isEnabled(), "Account must be disabled prior to OTP verification");

        // Verify invalid OTP fails
        boolean invalid = authService.verifyRegister("registrant@test.com", "000000");
        assertFalse(invalid);

        // Verify with token retrieval
        var tokenOpt = otpTokenRepository.findTopByEmailAndTypeOrderByCreatedAtDesc("registrant@test.com", "REGISTER");
        assertTrue(tokenOpt.isPresent());

        // Reset password flow
        authService.forgotPassword("registrant@test.com");
        var resetTokenOpt = otpTokenRepository.findTopByEmailAndTypeOrderByCreatedAtDesc("registrant@test.com", "RESET_PASSWORD");
        assertTrue(resetTokenOpt.isPresent());
    }
}
