package by.bsuir.authservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "aggregate_id", nullable = false)
	private String aggregateId;

	@Column(name = "aggregate_type", nullable = false, length = 100)
	private String aggregateType;

	@Column(name = "version", nullable = false)
	private Integer version;

	@Column(name = "event_type", nullable = false, length = 100)
	private String eventType;

	@Column(name = "payload", nullable = false, columnDefinition = "JSON")
	private String payload;

	@Column(name = "created_at", nullable = false)
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();
}
