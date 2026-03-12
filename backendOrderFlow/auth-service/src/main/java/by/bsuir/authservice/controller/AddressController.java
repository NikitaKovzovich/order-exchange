package by.bsuir.authservice.controller;

import by.bsuir.authservice.DTO.AddressDTO;
import by.bsuir.authservice.DTO.AddressManagementDTO;
import by.bsuir.authservice.DTO.AddressResponseDTO;
import by.bsuir.authservice.entity.Address;
import by.bsuir.authservice.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Tag(name = "Addresses", description = "Address management API")
public class AddressController {
	private final AddressService addressService;

	@GetMapping("/company/{companyId}")
	@Operation(summary = "Get all addresses for a company")
	public ResponseEntity<List<AddressResponseDTO>> getCompanyAddresses(@PathVariable Long companyId) {
		List<Address> addresses = addressService.getCompanyAddresses(companyId);
		List<AddressResponseDTO> response = addresses.stream()
				.map(this::toResponse)
				.collect(Collectors.toList());
		return ResponseEntity.ok(response);
	}

	@PostMapping
	@Operation(summary = "Add address to company")
	public ResponseEntity<AddressResponseDTO> addAddress(
			@RequestBody AddressManagementDTO dto,
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long companyId) {
		if (dto.getCompanyId() == null) {
			dto.setCompanyId(companyId);
		}
		Address address = addressService.addAddress(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(address));
	}

	@PostMapping("/bulk")
	@Operation(summary = "Add multiple addresses to company")
	public ResponseEntity<List<AddressResponseDTO>> addMultipleAddresses(
			@RequestBody List<AddressDTO> addressDTOs,
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long companyId) {
		List<Address> addresses = addressService.addMultipleAddresses(companyId, addressDTOs);
		List<AddressResponseDTO> response = addresses.stream()
				.map(this::toResponse)
				.collect(Collectors.toList());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PutMapping("/{addressId}")
	@Operation(summary = "Update address")
	public ResponseEntity<AddressResponseDTO> updateAddress(
			@PathVariable Long addressId,
			@RequestBody AddressManagementDTO dto) {
		Address address = addressService.updateAddress(addressId, dto);
		return ResponseEntity.ok(toResponse(address));
	}

	@DeleteMapping("/{addressId}")
	@Operation(summary = "Delete address")
	public ResponseEntity<Void> deleteAddress(@PathVariable Long addressId) {
		addressService.deleteAddress(addressId);
		return ResponseEntity.noContent().build();
	}

	private AddressResponseDTO toResponse(Address address) {
		return AddressResponseDTO.builder()
				.id(address.getId())
				.addressType(address.getAddressType().name())
				.fullAddress(address.getFullAddress())
				.isDefault(address.getIsDefault())
				.build();
	}
}
