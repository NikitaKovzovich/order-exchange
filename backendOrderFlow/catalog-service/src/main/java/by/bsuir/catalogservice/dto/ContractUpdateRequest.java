package by.bsuir.catalogservice.dto;

import java.time.LocalDate;




public record ContractUpdateRequest(
	String contractNumber,
	LocalDate contractDate,
	LocalDate contractEndDate
) {}
