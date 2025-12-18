package by.bsuir.authservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "responsible_person",
		uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "position"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponsiblePerson {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	@Column(name = "full_name", nullable = false)
	private String fullName;

	@Enumerated(EnumType.STRING)
	@Column(name = "position", nullable = false)
	private Position position;

	public enum Position {
		director,
		chief_accountant
	}
}
