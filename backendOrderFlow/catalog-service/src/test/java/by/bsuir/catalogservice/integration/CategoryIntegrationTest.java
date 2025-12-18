package by.bsuir.catalogservice.integration;

import by.bsuir.catalogservice.entity.Category;
import by.bsuir.catalogservice.repository.CategoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CategoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category parentCategory;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();

        parentCategory = categoryRepository.save(Category.builder()
                .name("Test Parent Category")
                .build());

        categoryRepository.save(Category.builder()
                .name("Test Child Category")
                .parent(parentCategory)
                .build());
    }

    @Test
    @DisplayName("Should get all categories")
    void shouldGetAllCategories() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("Should get category tree")
    void shouldGetCategoryTree() throws Exception {
        mockMvc.perform(get("/api/categories/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should get category by ID")
    void shouldGetCategoryById() throws Exception {
        mockMvc.perform(get("/api/categories/" + parentCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Test Parent Category"));
    }

    @Test
    @DisplayName("Should get subcategories")
    void shouldGetSubcategories() throws Exception {
        mockMvc.perform(get("/api/categories/" + parentCategory.getId() + "/subcategories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    @DisplayName("Should create category")
    void shouldCreateCategory() throws Exception {
        Map<String, Object> categoryRequest = Map.of(
                "name", "New Test Category"
        );

        mockMvc.perform(post("/api/categories")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("New Test Category"));
    }

    @Test
    @DisplayName("Should return 404 for non-existent category")
    void shouldReturn404ForNonExistentCategory() throws Exception {
        mockMvc.perform(get("/api/categories/99999"))
                .andExpect(status().isNotFound());
    }
}
