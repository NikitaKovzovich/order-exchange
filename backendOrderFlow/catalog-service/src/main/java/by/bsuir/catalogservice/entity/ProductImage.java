package by.bsuir.catalogservice.entity;

import jakarta.persistence.*;
import lombok.*;




@Entity
@Table(name = "product_image")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Lob
	@Column(name = "image_data", nullable = false, columnDefinition = "MEDIUMBLOB")
	private byte[] imageData;

	@Column(name = "is_primary")
	@Builder.Default
	private Boolean isPrimary = false;

	@Column(name = "mime_type", length = 50)
	private String mimeType;

	@Column(name = "file_name", length = 255)
	private String fileName;




	public long getSizeKb() {
		return imageData != null ? imageData.length / 1024 : 0;
	}




	public void setPrimary() {
		this.isPrimary = true;
	}




	public void unsetPrimary() {
		this.isPrimary = false;
	}
}
