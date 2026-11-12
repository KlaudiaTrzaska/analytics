package com.example.analitics.analytics;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class GrocerySpendingService {

	private final GrocerySpendingRepository repository;
	private final Clock clock;

	public GrocerySpendingService(GrocerySpendingRepository repository, Clock clock) {
		this.repository = repository;
		this.clock = clock;
	}

	public void record(GrocerySpendingEvent event) {
		Assert.notNull(event.date(), "date is required");
		Assert.notNull(event.amount(), "amount is required");

		repository.save(event.date(), event.amount());
	}

	public MonthlySpendingResponse currentMonthSpending() {
		YearMonth currentMonth = YearMonth.now(clock);
		LocalDate start = currentMonth.atDay(1);
		LocalDate end = currentMonth.plusMonths(1).atDay(1);
		BigDecimal amount = repository.sumBetween(start, end);

		return new MonthlySpendingResponse(currentMonth, amount);
	}
}
