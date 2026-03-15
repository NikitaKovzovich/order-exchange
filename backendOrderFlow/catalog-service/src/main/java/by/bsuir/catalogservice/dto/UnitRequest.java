package by.bsuir.catalogservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UnitRequest(
	@NotBlank(message = "Unit name is required")
	@Size(max = 20, message = "Unit name must not exceed 20 characters")
	String name
) {}
