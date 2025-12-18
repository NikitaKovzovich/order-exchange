package by.bsuir.authservice.service;

import by.bsuir.authservice.entity.Address;
import by.bsuir.authservice.entity.Company;
import by.bsuir.authservice.repository.AddressRepository;
import by.bsuir.authservice.repository.CompanyRepository;
import by.bsuir.authservice.DTO.AddressManagementDTO;
import by.bsuir.authservice.DTO.AddressDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {
	private final AddressRepository addressRepository;
	private final CompanyRepository companyRepository;

	@Transactional
	public Address addAddress(AddressManagementDTO dto) {
		Company company = companyRepository.findById(dto.getCompanyId())
				.orElseThrow(() -> new IllegalArgumentException("Company not found"));

		if (dto.getIsDefault() != null && dto.getIsDefault()) {
			List<Address> addresses = addressRepository.findByCompanyId(company.getId());
			for (Address address : addresses) {
				address.setIsDefault(false);
				addressRepository.save(address);
			}
		}

		Address address = Address.builder()
				.company(company)
				.addressType(Address.AddressType.valueOf(dto.getAddressType()))
				.fullAddress(dto.getFullAddress())
				.isDefault(dto.getIsDefault() != null && dto.getIsDefault())
				.build();

		return addressRepository.save(address);
	}

	@Transactional
	public List<Address> addMultipleAddresses(Long companyId, List<AddressDTO> addressDTOs) {
		Company company = companyRepository.findById(companyId)
				.orElseThrow(() -> new IllegalArgumentException("Company not found"));

		List<Address> addresses = new ArrayList<>();
		boolean hasDefault = false;

		for (AddressDTO dto : addressDTOs) {
			if (dto.getIsDefault() != null && dto.getIsDefault()) {
				if (hasDefault) {
					dto.setIsDefault(false);
				} else {
					hasDefault = true;
				}
			}

			addresses.add(Address.builder()
					.company(company)
					.addressType(Address.AddressType.valueOf(dto.getAddressType()))
					.fullAddress(dto.getFullAddress())
					.isDefault(dto.getIsDefault() != null && dto.getIsDefault())
					.build());
		}

		return addressRepository.saveAll(addresses);
	}

	@Transactional(readOnly = true)
	public List<Address> getCompanyAddresses(Long companyId) {
		return addressRepository.findByCompanyId(companyId);
	}

	@Transactional
	public Address updateAddress(Long addressId, AddressManagementDTO dto) {
		Address address = addressRepository.findById(addressId)
				.orElseThrow(() -> new IllegalArgumentException("Address not found"));

		if (dto.getIsDefault() != null && dto.getIsDefault()) {
			List<Address> addresses = addressRepository.findByCompanyId(address.getCompany().getId());
			for (Address addr : addresses) {
				addr.setIsDefault(false);
				addressRepository.save(addr);
			}
		}

		address.setAddressType(Address.AddressType.valueOf(dto.getAddressType()));
		address.setFullAddress(dto.getFullAddress());
		address.setIsDefault(dto.getIsDefault() != null && dto.getIsDefault());

		return addressRepository.save(address);
	}

	@Transactional
	public void deleteAddress(Long addressId) {
		addressRepository.deleteById(addressId);
	}
}
