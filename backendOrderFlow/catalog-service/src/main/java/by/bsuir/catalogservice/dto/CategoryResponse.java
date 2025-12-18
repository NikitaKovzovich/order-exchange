package by.bsuir.catalogservice.dto;

import java.util.List;

public record CategoryResponse(
	Long id,
	String name,
	Long parentId,
	String parentName,
	List<CategoryResponse> children,
	int productCount
) {
	public CategoryResponse(Long id, String name, Long parentId, String parentName, int productCount) {
		this(id, name, parentId, parentName, null, productCount);
	}
}
