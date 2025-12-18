package by.bsuir.catalogservice.repository;

import by.bsuir.catalogservice.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
	List<Event> findByAggregateIdOrderByVersionAsc(String aggregateId);

	@Query("SELECT MAX(e.version) FROM Event e WHERE e.aggregateId = :aggregateId")
	Optional<Integer> findMaxVersionByAggregateId(@Param("aggregateId") String aggregateId);

	List<Event> findByAggregateTypeAndEventType(String aggregateType, String eventType);
}
