package by.bsuir.orderservice.integration;

import by.bsuir.orderservice.entity.Cart;
import by.bsuir.orderservice.entity.CartItem;
import by.bsuir.orderservice.repository.CartRepository;
import by.bsuir.orderservice.service.EventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CartIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CartRepository cartRepository;

    @MockBean
    private EventPublisher eventPublisher;

    private Cart testCart;

    @BeforeEach
    void setUp() {
        cartRepository.deleteAll();

        testCart = Cart.builder()
                .customerId(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();

        CartItem testCartItem = CartItem.builder()
                .cart(testCart)
                .productId(1L)
                .supplierId(2L)
                .productName("Test Product")
                .productSku("TEST-001")
                .quantity(5)
                .unitPrice(new BigDecimal("100.00"))
                .build();
        testCartItem.calculateTotal();
        testCart.getItems().add(testCartItem);

        testCart = cartRepository.save(testCart);
    }

    @Test
    @DisplayName("Should get cart")
    void shouldGetCart() throws Exception {
        mockMvc.perform(get("/api/cart")
                        .header("X-User-Id", "1")
                        .header("X-User-Company-Id", "1")
                        .header("X-User-Role", "RETAIL_CHAIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }


    @Test
    @DisplayName("Should clear cart")
    void shouldClearCart() throws Exception {
        mockMvc.perform(delete("/api/cart")
                        .header("X-User-Id", "1")
                        .header("X-User-Company-Id", "1")
                        .header("X-User-Role", "RETAIL_CHAIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
