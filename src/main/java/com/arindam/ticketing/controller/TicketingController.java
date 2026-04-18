package com.arindam.ticketing.controller;

import com.arindam.ticketing.dto.BookingRequest;
import com.arindam.ticketing.entity.Booking;
import com.arindam.ticketing.entity.Event;
import com.arindam.ticketing.entity.Seat;
import com.arindam.ticketing.exception.SeatUnavailableException;
import com.arindam.ticketing.service.BookingService;
import com.arindam.ticketing.service.SeatLockingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class TicketingController {

    private final BookingService bookingService;
    private final SeatLockingService seatLockingService;

    // Requirement 1: Initialize the Event
    @PostMapping("/initialize")
    public ResponseEntity<String> initializeEvent(
            @RequestParam(defaultValue = "Dynamic Prototype Event") String eventName) {

        Event event = bookingService.initializeEvent(eventName);

        return ResponseEntity.ok(String.format("Successfully initialized event: '%s' (ID: %d) with 100 available seats.",
                event.getEventName(), event.getId()));
    }

    // Requirement 2: Get all seats
    @GetMapping("/seats")
    public ResponseEntity<List<Seat>> getSeats(@RequestParam(defaultValue = "1") Long eventId) {
        List<Seat> seats = bookingService.getAllSeats(eventId);
        return ResponseEntity.ok(seats);
    }

    // Requirement 3: Book Mentioned seats
    @PostMapping("/book")
    public ResponseEntity<?> bookTickets(@RequestBody BookingRequest request) {
        List<Integer> successfullyLockedSeats = new ArrayList<>();

        try {
            for (Integer seatNo : request.getSeats()) {
                boolean locked = seatLockingService.lockSeat(request.getEventId(), seatNo, request.getUserName());
                if (locked) {
                    successfullyLockedSeats.add(seatNo);
                } else {
                    // If even ONE seat fails, we must rollback the locks we already acquired
                    rollbackRedisLocks(request.getEventId(), successfullyLockedSeats);
                    throw new SeatUnavailableException("Checkout failed: Seat " + seatNo + " is currently locked by another user.");
                }
            }

            Booking finalReceipt = bookingService.processBooking(request.getEventId(), request.getUserName(), request.getSeats());
            return ResponseEntity.ok(finalReceipt);

        }  finally {
            rollbackRedisLocks(request.getEventId(), successfullyLockedSeats);
        }
    }

    private void rollbackRedisLocks(Long eventId, List<Integer> lockedSeats) {
        for (Integer seatNo : lockedSeats) {
            seatLockingService.unlockSeat(eventId, seatNo);
        }
    }

    // Self: Delete an Event
    @DeleteMapping("/{eventId}")
    public ResponseEntity<String> deleteEvent(@PathVariable Long eventId) {

        bookingService.deleteEvent(eventId);

        return ResponseEntity.ok("Successfully deleted Event ID: " + eventId + " and all associated seats.");
    }
}
