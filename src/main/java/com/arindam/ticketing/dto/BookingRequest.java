package com.arindam.ticketing.dto;

import lombok.Data;
import java.util.List;

@Data
public class BookingRequest {
    private Long eventId;
    private String userName;
    private List<Integer> seats;
}