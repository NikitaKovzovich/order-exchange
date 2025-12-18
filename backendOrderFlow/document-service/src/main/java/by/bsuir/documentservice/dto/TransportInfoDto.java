package by.bsuir.documentservice.dto;

public record TransportInfoDto(
		String vehicleModel,
		String vehicleNumber,
		String driverName,
		String driverLicense,
		String waybillNumber,
		String carrierName
) {}
