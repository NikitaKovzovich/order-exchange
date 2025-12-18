package by.bsuir.catalogservice.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Единица измерения (Read Model для CQRS)
 * Справочная таблица
 */
@Entity
@Table(name = "unit_of_measure")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitOfMeasure {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 20)
	private String name;

	/**
	 * Получить сокращенное обозначение
	 */
	public String getAbbreviation() {
		return switch (name.toLowerCase()) {
			case "штука" -> "шт.";
			case "килограмм" -> "кг";
			case "литр" -> "л";
			case "метр" -> "м";
			case "упаковка" -> "уп.";
			case "коробка" -> "кор.";
			case "паллета" -> "пал.";
			default -> name;
		};
	}
}
