package by.bsuir.catalogservice.repository;

import by.bsuir.catalogservice.entity.UnitOfMeasure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasure, Long> {
	Optional<UnitOfMeasure> findByName(String name);
	boolean existsByName(String name);
}
