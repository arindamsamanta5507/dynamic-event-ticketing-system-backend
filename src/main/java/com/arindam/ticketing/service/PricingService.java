package com.arindam.ticketing.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class PricingService {

    public BigDecimal calculatePrice(int currentTicketsSold, int requestedQuantity) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        int simulatedSold = currentTicketsSold; // 0-based index of how many are sold

        for (int i = 0; i < requestedQuantity; i++) {

            if (simulatedSold < 50) {
                totalPrice = totalPrice.add(new BigDecimal("50.00"));
            }

            else if (simulatedSold < 80) {
                totalPrice = totalPrice.add(new BigDecimal("75.00"));
            }

            else {
                totalPrice = totalPrice.add(new BigDecimal("100.00"));
            }

            simulatedSold++; // Increment to evaluate the next ticket in the cart
        }
        return totalPrice;
    }
}
