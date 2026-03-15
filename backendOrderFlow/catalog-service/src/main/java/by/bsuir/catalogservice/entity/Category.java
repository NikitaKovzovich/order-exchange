package by.bsuir.catalogservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;





@Entity
@Table(name = "category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private Category parent;

	@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@Builder.Default
	private List<Category> children = new ArrayList<>();

	@OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
	@Builder.Default
	private List<Product> products = new ArrayList<>();




	public String getFullPath() {
		if (parent == null) {
			return name;
		}
		return parent.getFullPath() + " > " + name;
	}




	public boolean isRoot() {
		return parent == null;
	}




	public boolean isLeaf() {
		return children == null || children.isEmpty();
	}
}
