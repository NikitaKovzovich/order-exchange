package by.bsuir.catalogservice.service;

import by.bsuir.catalogservice.dto.ContractUpdateRequest;
import by.bsuir.catalogservice.dto.PartnershipRequest;
import by.bsuir.catalogservice.dto.PartnershipResponse;
import by.bsuir.catalogservice.entity.Partnership;
import by.bsuir.catalogservice.exception.DuplicateResourceException;
import by.bsuir.catalogservice.exception.InvalidOperationException;
import by.bsuir.catalogservice.exception.ResourceNotFoundException;
import by.bsuir.catalogservice.repository.PartnershipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PartnershipService Tests")
class PartnershipServiceTest {

	@Mock
	private PartnershipRepository partnershipRepository;

	@Mock
	private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

	@InjectMocks
	private PartnershipService partnershipService;

	private Partnership testPartnership;

	@BeforeEach
	void setUp() {
		testPartnership = Partnership.builder()
				.id(1L)
				.supplierId(100L)
				.customerId(200L)
				.status(Partnership.PartnershipStatus.PENDING)
				.contractNumber("C-001")
				.contractDate(LocalDate.of(2026, 1, 1))
				.contractEndDate(LocalDate.of(2027, 1, 1))
				.customerCompanyName("Торговая сеть А")
				.customerUnp("123456789")
				.supplierCompanyName("Поставщик Б")
				.createdAt(LocalDateTime.now())
				.build();
	}

	@Nested
	@DisplayName("createPartnershipRequest")
	class CreatePartnershipRequestTests {

		@Test
		@DisplayName("Должен создать заявку на партнёрство")
		void shouldCreatePartnershipRequest() {
			PartnershipRequest request = new PartnershipRequest(
					100L, "C-001", LocalDate.of(2026, 1, 1),
					LocalDate.of(2027, 1, 1), "Торговая сеть А", "123456789"
			);

			when(partnershipRepository.existsBySupplierIdAndCustomerId(100L, 200L)).thenReturn(false);
			when(partnershipRepository.save(any(Partnership.class))).thenAnswer(i -> {
				Partnership p = i.getArgument(0);
				p.setId(1L);
				return p;
			});

			PartnershipResponse response = partnershipService.createPartnershipRequest(200L, request);

			assertThat(response).isNotNull();
			assertThat(response.supplierId()).isEqualTo(100L);
			assertThat(response.customerId()).isEqualTo(200L);
			assertThat(response.status()).isEqualTo("PENDING");
			assertThat(response.contractNumber()).isEqualTo("C-001");
			assertThat(response.customerCompanyName()).isEqualTo("Торговая сеть А");
			verify(partnershipRepository).save(any(Partnership.class));
		}

		@Test
		@DisplayName("Должен выбросить исключение при дубликате партнёрства")
		void shouldThrowOnDuplicate() {
			PartnershipRequest request = new PartnershipRequest(
					100L, "C-001", null, null, "Торговая сеть А", "123456789"
			);

			when(partnershipRepository.existsBySupplierIdAndCustomerId(100L, 200L)).thenReturn(true);

			assertThatThrownBy(() -> partnershipService.createPartnershipRequest(200L, request))
					.isInstanceOf(DuplicateResourceException.class);

			verify(partnershipRepository, never()).save(any());
		}
	}

	@Nested
	@DisplayName("acceptPartnership")
	class AcceptPartnershipTests {

		@Test
		@DisplayName("Должен подтвердить заявку → ACTIVE")
		void shouldAcceptPartnership() {
			when(partnershipRepository.findById(1L)).thenReturn(Optional.of(testPartnership));
			when(partnershipRepository.save(any(Partnership.class))).thenAnswer(i -> i.getArgument(0));

			PartnershipResponse response = partnershipService.acceptPartnership(1L, 100L);

			assertThat(response.status()).isEqualTo("ACTIVE");
			verify(partnershipRepository).save(any(Partnership.class));
		}

		@Test
		@DisplayName("Должен выбросить исключение если партнёрство не принадлежит поставщику")
		void shouldThrowIfNotOwner() {
			when(partnershipRepository.findById(1L)).thenReturn(Optional.of(testPartnership));

			assertThatThrownBy(() -> partnershipService.acceptPartnership(1L, 999L))
					.isInstanceOf(InvalidOperationException.class);
		}

		@Test
		@DisplayName("Должен выбросить исключение если партнёрство не найдено")
		void shouldThrowIfNotFound() {
			when(partnershipRepository.findById(1L)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> partnershipService.acceptPartnership(1L, 100L))
					.isInstanceOf(ResourceNotFoundException.class);
		}

		@Test
		@DisplayName("Должен выбросить исключение если статус не PENDING")
		void shouldThrowIfNotPending() {
			testPartnership.setStatus(Partnership.PartnershipStatus.ACTIVE);
			when(partnershipRepository.findById(1L)).thenReturn(Optional.of(testPartnership));

			assertThatThrownBy(() -> partnershipService.acceptPartnership(1L, 100L))
					.isInstanceOf(IllegalStateException.class);
		}
	}

	@Nested
	@DisplayName("rejectPartnership")
	class RejectPartnershipTests {

		@Test
		@DisplayName("Должен отклонить заявку → REJECTED")
		void shouldRejectPartnership() {
			when(partnershipRepository.findById(1L)).thenReturn(Optional.of(testPartnership));
			when(partnershipRepository.save(any(Partnership.class))).thenAnswer(i -> i.getArgument(0));

			PartnershipResponse response = partnershipService.rejectPartnership(1L, 100L);

			assertThat(response.status()).isEqualTo("REJECTED");
		}

		@Test
		@DisplayName("Должен выбросить исключение при отклонении уже активного")
		void shouldThrowIfActive() {
			testPartnership.setStatus(Partnership.PartnershipStatus.ACTIVE);
			when(partnershipRepository.findById(1L)).thenReturn(Optional.of(testPartnership));

			assertThatThrownBy(() -> partnershipService.rejectPartnership(1L, 100L))
					.isInstanceOf(IllegalStateException.class);
		}
	}

	@Nested
	@DisplayName("updateContract")
	class UpdateContractTests {

		@Test
		@DisplayName("Должен обновить данные договора")
		void shouldUpdateContract() {
			when(partnershipRepository.findById(1L)).thenReturn(Optional.of(testPartnership));
			when(partnershipRepository.save(any(Partnership.class))).thenAnswer(i -> i.getArgument(0));

			ContractUpdateRequest request = new ContractUpdateRequest(
					"C-002", LocalDate.of(2026, 6, 1), LocalDate.of(2027, 6, 1)
			);

			PartnershipResponse response = partnershipService.updateContract(1L, 100L, request);

			assertThat(response.contractNumber()).isEqualTo("C-002");
			assertThat(response.contractDate()).isEqualTo(LocalDate.of(2026, 6, 1));
			assertThat(response.contractEndDate()).isEqualTo(LocalDate.of(2027, 6, 1));
		}
	}

	@Nested
	@DisplayName("List methods")
	class ListTests {

		@Test
		@DisplayName("Должен вернуть ожидающие заявки поставщика")
		void shouldReturnPendingRequests() {
			when(partnershipRepository.findBySupplierIdAndStatus(100L, Partnership.PartnershipStatus.PENDING))
					.thenReturn(List.of(testPartnership));

			List<PartnershipResponse> result = partnershipService.getSupplierPendingRequests(100L);

			assertThat(result).hasSize(1);
			assertThat(result.get(0).status()).isEqualTo("PENDING");
		}

		@Test
		@DisplayName("Должен вернуть активные договоры поставщика")
		void shouldReturnActivePartners() {
			testPartnership.setStatus(Partnership.PartnershipStatus.ACTIVE);
			when(partnershipRepository.findBySupplierIdAndStatus(100L, Partnership.PartnershipStatus.ACTIVE))
					.thenReturn(List.of(testPartnership));

			List<PartnershipResponse> result = partnershipService.getSupplierActivePartners(100L);

			assertThat(result).hasSize(1);
			assertThat(result.get(0).status()).isEqualTo("ACTIVE");
		}

		@Test
		@DisplayName("Должен вернуть все партнёрства поставщика")
		void shouldReturnAllSupplierPartnerships() {
			when(partnershipRepository.findBySupplierId(100L)).thenReturn(List.of(testPartnership));

			List<PartnershipResponse> result = partnershipService.getSupplierPartnerships(100L);

			assertThat(result).hasSize(1);
		}

		@Test
		@DisplayName("Должен вернуть партнёрства торговой сети")
		void shouldReturnCustomerPartnerships() {
			when(partnershipRepository.findByCustomerId(200L)).thenReturn(List.of(testPartnership));

			List<PartnershipResponse> result = partnershipService.getCustomerPartnerships(200L);

			assertThat(result).hasSize(1);
		}

		@Test
		@DisplayName("Должен вернуть ID активных поставщиков")
		void shouldReturnActiveSupplierIds() {
			when(partnershipRepository.findActiveSupplierIdsByCustomerId(200L))
					.thenReturn(List.of(100L, 300L));

			List<Long> ids = partnershipService.getActiveSupplierIds(200L);

			assertThat(ids).containsExactly(100L, 300L);
		}

		@Test
		@DisplayName("Должен вернуть пустой список если нет партнёрств")
		void shouldReturnEmptyList() {
			when(partnershipRepository.findBySupplierId(999L)).thenReturn(List.of());

			List<PartnershipResponse> result = partnershipService.getSupplierPartnerships(999L);

			assertThat(result).isEmpty();
		}
	}
}
