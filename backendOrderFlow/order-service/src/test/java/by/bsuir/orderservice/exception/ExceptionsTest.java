package by.bsuir.orderservice.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionsTest {

	@Test
	@DisplayName("ResourceNotFoundException should contain resource info")
	void resourceNotFoundExceptionShouldContainResourceInfo() {
		ResourceNotFoundException ex = new ResourceNotFoundException("Order", "id", 123L);

		assertThat(ex.getMessage()).contains("Order");
		assertThat(ex.getMessage()).contains("id");
		assertThat(ex.getMessage()).contains("123");
	}

	@Test
	@DisplayName("InvalidStateTransitionException should contain states")
	void invalidStateTransitionExceptionShouldContainStates() {
		InvalidStateTransitionException ex = new InvalidStateTransitionException("PENDING", "SHIPPED");

		assertThat(ex.getMessage()).contains("PENDING");
		assertThat(ex.getMessage()).contains("SHIPPED");
	}

	@Test
	@DisplayName("InvalidOperationException should contain operation and message")
	void invalidOperationExceptionShouldContainOperationAndMessage() {
		InvalidOperationException ex = new InvalidOperationException("confirm", "Not allowed");

		assertThat(ex.getMessage()).contains("confirm");
		assertThat(ex.getMessage()).contains("Not allowed");
		assertThat(ex.getOperation()).isEqualTo("confirm");
	}
}
