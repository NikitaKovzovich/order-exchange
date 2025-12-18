package by.bsuir.orderservice.integration;

import by.bsuir.orderservice.repository.OrderRepository;
import by.bsuir.orderservice.repository.OrderStatusRepository;
import by.bsuir.orderservice.service.EventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AnalyticsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderStatusRepository statusRepository;

    @MockBean
    private EventPublisher eventPublisher;

    @Test
    @DisplayName("Should get overall analytics")
    void shouldGetOverallAnalytics() throws Exception {
        mockMvc.perform(get("/api/analytics")
                        .header("X-User-Id", "1")
                        .header("X-User-Company-Id", "1")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should get supplier analytics")
    void shouldGetSupplierAnalytics() throws Exception {
        mockMvc.perform(get("/api/analytics/supplier")
                        .header("X-User-Id", "1")
                        .header("X-User-Company-Id", "1")
                        .header("X-User-Role", "SUPPLIER")
                        .param("period", "month"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should get customer analytics")
    void shouldGetCustomerAnalytics() throws Exception {
        mockMvc.perform(get("/api/analytics/customer")
                        .header("X-User-Id", "1")
                        .header("X-User-Company-Id", "2")
                        .header("X-User-Role", "RETAIL_CHAIN")
                        .param("period", "month"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should get product purchase history")
    void shouldGetProductPurchaseHistory() throws Exception {
        mockMvc.perform(get("/api/analytics/customer/product-history")
                        .header("X-User-Id", "1")
                        .header("X-User-Company-Id", "2")
                        .header("X-User-Role", "RETAIL_CHAIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
