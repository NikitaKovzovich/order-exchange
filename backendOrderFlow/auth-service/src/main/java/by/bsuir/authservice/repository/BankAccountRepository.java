package by.bsuir.authservice.repository;

import by.bsuir.authservice.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
	Optional<BankAccount> findByCompanyId(Long companyId);
}
