package by.bsuir.authservice.repository;

import by.bsuir.authservice.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
	List<Address> findByCompanyId(Long companyId);
}
