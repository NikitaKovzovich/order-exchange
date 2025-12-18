package by.bsuir.authservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RetailChainAddressesRequest {
	private Long companyId;
	private List<AddressDTO> addresses;
}
