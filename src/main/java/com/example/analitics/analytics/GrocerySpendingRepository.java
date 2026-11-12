package com.example.analitics.analytics;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class GrocerySpendingRepository {

	private final JdbcTemplate jdbcTemplate;

	public GrocerySpendingRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void save(LocalDate date, BigDecimal amount) {
		jdbcTemplate.update(
				"INSERT INTO analytics (date, amount) VALUES (?, ?)",
				date,
				amount);
	}

	public BigDecimal sumBetween(LocalDate startInclusive, LocalDate endExclusive) {
		BigDecimal total = jdbcTemplate.queryForObject(
				"""
				SELECT COALESCE(SUM(amount), 0)
				FROM analytics
				WHERE date >= ? AND date < ?
				""",
				BigDecimal.class,
				startInclusive,
				endExclusive);

		return total == null ? BigDecimal.ZERO : total;
	}
}
