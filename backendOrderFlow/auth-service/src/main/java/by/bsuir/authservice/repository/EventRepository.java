package by.bsuir.authservice.repository;

import by.bsuir.authservice.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

	List<Event> findByAggregateIdOrderByVersionAsc(String aggregateId);

	@Query("SELECT COALESCE(MAX(e.version), 0) FROM Event e WHERE e.aggregateId = :aggregateId")
	Integer findMaxVersionByAggregateId(String aggregateId);

	Optional<Event> findFirstByAggregateIdOrderByVersionDesc(String aggregateId);
	Page<Event> findAllByOrderByCreatedAtDesc(Pageable pageable);
	Page<Event> findByAggregateTypeAndAggregateIdOrderByCreatedAtDesc(
			String aggregateType, String aggregateId, Pageable pageable);
	@Query("SELECT e FROM Event e WHERE e.aggregateId IN :ids ORDER BY e.createdAt DESC")
	Page<Event> findByAggregateIdInOrderByCreatedAtDesc(@Param("ids") List<String> ids, Pageable pageable);
}
