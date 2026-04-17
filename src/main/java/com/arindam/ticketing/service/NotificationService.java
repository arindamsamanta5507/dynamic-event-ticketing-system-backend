package com.arindam.ticketing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC = "booking-notifications";

    public void sendBookingConfirmation(String userName, int tickets, BigDecimal price) {

        String message = String.format("SUCCESS: Booking confirmed for %s. %d tickets. Total Paid: $%s",
                userName, tickets, price.toString());


        kafkaTemplate.send(TOPIC, userName, message);
        log.info("Produced Kafka Event -> Topic: {}, Message: {}", TOPIC, message);
    }
}
