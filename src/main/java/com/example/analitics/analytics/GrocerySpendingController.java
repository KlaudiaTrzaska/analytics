package com.example.analitics.analytics;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/groceries/spending")
public class GrocerySpendingController {

	private final GrocerySpendingService service;

	public GrocerySpendingController(GrocerySpendingService service) {
		this.service = service;
	}

	@GetMapping("/current-month")
	public MonthlySpendingResponse currentMonthSpending() {
		return service.currentMonthSpending();
	}
}
