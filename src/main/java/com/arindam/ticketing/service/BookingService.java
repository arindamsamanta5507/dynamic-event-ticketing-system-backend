package com.arindam.ticketing.service;

import com.arindam.ticketing.entity.*;
import com.arindam.ticketing.exception.ResourceNotFoundException;
import com.arindam.ticketing.exception.SeatUnavailableException;
import com.arindam.ticketing.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final PricingService pricingService;
    private final NotificationService notificationService;

    /**
     * Requirement 1: POST /initialize
     * Initializes an event with exactly 100 seats.
     */
    @Transactional
    public Event initializeEvent(String eventName) {

        Event event = Event.builder()
                .eventName(eventName)
                .totalCapacity(100)
                .ticketsSold(0)
                .build();
        Event savedEvent = eventRepository.save(event);

        // 2. Generate exactly 100 Available Seats linked to this event
        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            seats.add(Seat.builder()
                    .seatNumber(i)
                    .status(SeatStatus.AVAILABLE)
                    .event(savedEvent)
                    .build());
        }
        seatRepository.saveAll(seats);

        log.info("Initialized Event {} with 100 seats.", savedEvent.getId());
        return savedEvent;
    }

    /**
     * Requirement 2: GET /seats
     */
    public List<Seat> getAllSeats(Long eventId) {
        return seatRepository.findByEventId(eventId);
    }

    /**
     * Requirement 3: POST /book
     */

    @Transactional
    public Booking processBooking(Long eventId, String userName, List<Integer> requestedSeatNumbers) {
        log.info("User {} attempting to book seats: {}", userName, requestedSeatNumbers);

        // Freeze the event row so no other thread can read or write it
        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event with ID " + eventId + " not found"));

        // Validate System Capacity
        if (event.getTicketsSold() + requestedSeatNumbers.size() > event.getTotalCapacity()) {
            throw new SeatUnavailableException("Not enough total tickets remaining. Only " +
                    (event.getTotalCapacity() - event.getTicketsSold()) + " left.");
        }

        //Fetch and Validate Specific Seats
        List<Seat> seats = seatRepository.findByEventIdAndSeatNumberIn(eventId, requestedSeatNumbers);
        if (seats.size() != requestedSeatNumbers.size()) {
            throw new RuntimeException("Invalid seat numbers provided.");
        }
        for (Seat seat : seats) {
            if (seat.getStatus() == SeatStatus.BOOKED) {
                throw new SeatUnavailableException("Seat " + seat.getSeatNumber() + " was just booked by someone else!");
            }
        }

        //Calculate Price (Using our pure Java function)
        BigDecimal totalPrice = pricingService.calculatePrice(event.getTicketsSold(), requestedSeatNumbers.size());

        //Update the Master Event Counter
        event.setTicketsSold(event.getTicketsSold() + requestedSeatNumbers.size());
        eventRepository.save(event);

        //Generate the Receipt
        Booking booking = Booking.builder()
                .userName(userName)
                .totalPrice(totalPrice)
                .build();
        Booking savedBooking = bookingRepository.save(booking);

        //Mark Seats as Booked and link them to the receipt
        for (Seat seat : seats) {
            seat.setStatus(SeatStatus.BOOKED);
            seat.setBooking(savedBooking);
        }
        seatRepository.saveAll(seats);

        //Trigger Async Side Effects (Kafka)
        notificationService.sendBookingConfirmation(userName, requestedSeatNumbers.size(), totalPrice);

        log.info("Booking completely successfully! ID: {}", savedBooking.getId());
        return savedBooking;
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        //Verify the event actually exists, or throw our custom 404
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event with ID " + eventId + " not found"));

        //Fetch all seats linked to this event
        List<Seat> seats = seatRepository.findByEventId(eventId);

        //Delete the children (Seats) first to satisfy MySQL Foreign Key constraints
        if (!seats.isEmpty()) {
            seatRepository.deleteAll(seats);
        }

        //Delete the parent (Event)
        eventRepository.delete(event);

        log.info("Successfully deleted Event ID: {} and its {} associated seats.", eventId, seats.size());
    }
}
