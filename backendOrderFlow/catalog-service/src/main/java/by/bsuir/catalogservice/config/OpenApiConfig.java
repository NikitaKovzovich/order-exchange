package by.bsuir.catalogservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI catalogServiceOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Catalog Service API")
						.description("API for managing products, categories, and inventory")
						.version("1.0.0"))
				.servers(List.of(
						new Server().url("http://localhost:8082").description("Local server"),
						new Server().url("http://localhost:8765").description("API Gateway")
				));
	}
}
