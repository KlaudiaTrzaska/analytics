package com.example.analitics.analytics;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.awspring.cloud.sqs.annotation.SqsListener;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.ObjectMapper;

@Component
public class GrocerySpendingListener {

	private static final Logger log = LoggerFactory.getLogger(GrocerySpendingListener.class);

	private final GrocerySpendingService service;
	private final ObjectMapper objectMapper;

	public GrocerySpendingListener(GrocerySpendingService service, ObjectMapper objectMapper) {
		this.service = service;
		this.objectMapper = objectMapper;
	}

	@SqsListener("${aws.sqs.analytics-queue}")
	public void receive(String payload) {
		log.info("Received grocery spending event: {}", payload);
		GrocerySpendingEvent event = parseEvent(payload);
		service.record(event);
	}

	private GrocerySpendingEvent parseEvent(String payload) {
		try {
			ExtractedSpending extracted = extractSpending(payload);

			if (!extracted.hasSpending() && extracted.message() != null) {
				extracted = extractSpending(extracted.message());
			}

			if (extracted.date() == null) {
				throw new IllegalArgumentException("date is required");
			}
			if (extracted.amount() == null) {
				throw new IllegalArgumentException("amount is required");
			}

			return new GrocerySpendingEvent(
					LocalDate.parse(extracted.date()),
					new BigDecimal(extracted.amount()));
		}
		catch (RuntimeException exception) {
			throw new IllegalArgumentException("Invalid grocery spending event payload: " + payload, exception);
		}
	}

	private ExtractedSpending extractSpending(String payload) {
		String date = null;
		String amount = null;
		String message = null;

		try (JsonParser parser = objectMapper.tokenStreamFactory().createParser(payload)) {
			while (parser.nextToken() != null) {
				if (parser.currentToken() != JsonToken.PROPERTY_NAME) {
					continue;
				}

				String fieldName = parser.currentName();
				parser.nextToken();

				if ("date".equals(fieldName) || "checkoutDate".equals(fieldName)) {
					date = parser.getText();
				}
				else if ("amount".equals(fieldName) || "totalPrice".equals(fieldName)) {
					amount = parser.getText();
				}
				else if ("Message".equals(fieldName)) {
					message = parser.getText();
				}

				if (date != null && amount != null) {
					break;
				}
			}
		}

		return new ExtractedSpending(date, amount, message);
	}

	private record ExtractedSpending(String date, String amount, String message) {

		private boolean hasSpending() {
			return date != null && amount != null;
		}
	}
}
