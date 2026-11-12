package com.example.analitics;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import com.example.analitics.analytics.GrocerySpendingEvent;
import com.example.analitics.analytics.GrocerySpendingListener;
import com.example.analitics.analytics.GrocerySpendingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockMakers;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.withSettings;

class DemoApplicationTests {

	private GrocerySpendingService service;
	private ObjectMapper objectMapper;
	private GrocerySpendingListener listener;

	@BeforeEach
	void setUp() {
		service = mock(GrocerySpendingService.class, withSettings().mockMaker(MockMakers.SUBCLASS));
		objectMapper = new ObjectMapper();
		listener = new GrocerySpendingListener(service, objectMapper);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"{\"date\":\"2026-11-12\",\"amount\":42.50}",
			"{\"checkoutDate\":\"2026-11-12\",\"totalPrice\":42.50}",
			"{\"amount\":\"42.50\",\"date\":\"2026-11-12\"}"
	})
	void recordsSpendingFromSupportedEventFormats(String payload) {
		listener.receive(payload);

		verify(service).record(new GrocerySpendingEvent(
				LocalDate.of(2026, 11, 12), new BigDecimal("42.50")));
		verifyNoMoreInteractions(service);
	}

	@Test
	void recordsSpendingFromSnsEnvelope() {
		String payload = objectMapper.writeValueAsString(Map.of(
				"Type", "Notification",
				"Message", "{\"checkoutDate\":\"2026-11-12\",\"totalPrice\":42.50}"));

		listener.receive(payload);

		verify(service).record(new GrocerySpendingEvent(
				LocalDate.of(2026, 11, 12), new BigDecimal("42.50")));
		verifyNoMoreInteractions(service);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"{\"amount\":42.50}",
			"{\"date\":\"2026-11-12\"}",
			"{}",
			"not json",
			"{\"date\":",
			"{\"date\":\"2026-02-30\",\"amount\":42.50}",
			"{\"date\":\"2026-11-12\",\"amount\":\"invalid\"}",
			"{\"Message\":\"not json\"}"
	})
	void rejectsInvalidEventsWithoutRecordingSpending(String payload) {
		assertThrows(IllegalArgumentException.class, () -> listener.receive(payload));

		verifyNoInteractions(service);
	}
}
