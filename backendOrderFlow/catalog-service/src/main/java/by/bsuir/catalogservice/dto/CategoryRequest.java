package by.bsuir.catalogservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
	@NotBlank(message = "Category name is required")
	@Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
	String name,
	Long parentId
) {}
