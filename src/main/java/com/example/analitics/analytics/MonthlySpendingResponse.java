package com.example.analitics.analytics;

import java.math.BigDecimal;
import java.time.YearMonth;

public record MonthlySpendingResponse(YearMonth month, BigDecimal amount) {
}
