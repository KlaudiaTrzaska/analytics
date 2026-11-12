package com.example.analitics.analytics;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GrocerySpendingEvent(LocalDate date, BigDecimal amount) {
}
