package by.bsuir.authservice.repository;

import by.bsuir.authservice.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

	List<Event> findByAggregateIdOrderByVersionAsc(String aggregateId);

	@Query("SELECT COALESCE(MAX(e.version), 0) FROM Event e WHERE e.aggregateId = :aggregateId")
	Integer findMaxVersionByAggregateId(String aggregateId);

	Optional<Event> findFirstByAggregateIdOrderByVersionDesc(String aggregateId);
}
