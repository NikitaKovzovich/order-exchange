package by.bsuir.catalogservice.repository;

import by.bsuir.catalogservice.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
	List<Category> findByParentIsNull();
	List<Category> findByParentId(Long parentId);
	boolean existsByName(String name);

	@Query("SELECT c FROM Category c LEFT JOIN FETCH c.children WHERE c.parent IS NULL")
	List<Category> findAllRootWithChildren();
}
