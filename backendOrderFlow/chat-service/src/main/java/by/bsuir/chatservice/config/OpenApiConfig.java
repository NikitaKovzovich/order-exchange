package by.bsuir.chatservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI chatServiceOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("OrderFlow Chat Service API")
						.description("API для управления чатами между поставщиками и покупателями")
						.version("1.0.0"));
	}
}
