package by.bsuir.authservice.service;

import by.bsuir.authservice.entity.Address;
import by.bsuir.authservice.entity.Company;
import by.bsuir.authservice.repository.AddressRepository;
import by.bsuir.authservice.repository.CompanyRepository;
import by.bsuir.authservice.DTO.AddressManagementDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

	@Mock
	private AddressRepository addressRepository;

	@Mock
	private CompanyRepository companyRepository;

	@InjectMocks
	private AddressService addressService;

	private Company testCompany;
	private Address testAddress;

	@BeforeEach
	void setUp() {
		testCompany = Company.builder()
				.id(1L)
				.name("Test Company")
				.build();

		testAddress = Address.builder()
				.id(1L)
				.company(testCompany)
				.addressType(Address.AddressType.legal)
				.fullAddress("Test Street 1, Minsk")
				.isDefault(true)
				.build();
	}

	@Nested
	@DisplayName("Add Address Tests")
	class AddAddressTests {

		@Test
		@DisplayName("Should add new address")
		void shouldAddNewAddress() {
			AddressManagementDTO dto = new AddressManagementDTO();
			dto.setCompanyId(1L);
			dto.setAddressType("legal");
			dto.setFullAddress("New Address");
			dto.setIsDefault(false);

			when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));
			when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

			Address result = addressService.addAddress(dto);

			assertThat(result).isNotNull();
		}

		@Test
		@DisplayName("Should throw when company not found")
		void shouldThrowWhenCompanyNotFound() {
			AddressManagementDTO dto = new AddressManagementDTO();
			dto.setCompanyId(999L);
			dto.setAddressType("legal");

			when(companyRepository.findById(999L)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> addressService.addAddress(dto))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("Company not found");
		}
	}

	@Nested
	@DisplayName("Get Addresses Tests")
	class GetAddressesTests {

		@Test
		@DisplayName("Should get all company addresses")
		void shouldGetAllCompanyAddresses() {
			when(addressRepository.findByCompanyId(1L))
					.thenReturn(List.of(testAddress));

			List<Address> addresses = addressRepository.findByCompanyId(1L);

			assertThat(addresses).hasSize(1);
		}
	}
}
