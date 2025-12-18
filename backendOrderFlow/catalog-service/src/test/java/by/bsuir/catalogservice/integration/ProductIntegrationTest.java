package by.bsuir.catalogservice.integration;

import by.bsuir.catalogservice.entity.Category;
import by.bsuir.catalogservice.entity.Product;
import by.bsuir.catalogservice.entity.UnitOfMeasure;
import by.bsuir.catalogservice.entity.VatRate;
import by.bsuir.catalogservice.repository.CategoryRepository;
import by.bsuir.catalogservice.repository.ProductRepository;
import by.bsuir.catalogservice.repository.UnitOfMeasureRepository;
import by.bsuir.catalogservice.repository.VatRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UnitOfMeasureRepository unitRepository;

    @Autowired
    private VatRateRepository vatRateRepository;

    private Category testCategory;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        testCategory = categoryRepository.save(Category.builder()
                .name("Integration Test Category")
                .build());

        UnitOfMeasure unit = unitRepository.save(UnitOfMeasure.builder()
                .name("тестштука")
                .build());

        VatRate vatRate = vatRateRepository.save(VatRate.builder()
                .ratePercentage(new BigDecimal("20.00"))
                .description("Тестовый НДС 20%")
                .build());

        testProduct = productRepository.save(Product.builder()
                .name("Integration Test Product")
                .description("Test product description")
                .sku("INT-TEST-SKU-001")
                .pricePerUnit(new BigDecimal("99.99"))
                .category(testCategory)
                .unit(unit)
                .vatRate(vatRate)
                .supplierId(1L)
                .status(Product.ProductStatus.PUBLISHED)
                .build());
    }

    @Test
    @DisplayName("Should search products")
    void shouldSearchProducts() throws Exception {
        mockMvc.perform(get("/api/products/search")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("Should get product by ID")
    void shouldGetProductById() throws Exception {
        mockMvc.perform(get("/api/products/" + testProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Integration Test Product"));
    }

    @Test
    @DisplayName("Should return 404 for non-existent product")
    void shouldReturn404ForNonExistentProduct() throws Exception {
        mockMvc.perform(get("/api/products/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get supplier products")
    void shouldGetSupplierProducts() throws Exception {
        mockMvc.perform(get("/api/products/supplier")
                        .header("X-User-Company-Id", "1")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }
}
