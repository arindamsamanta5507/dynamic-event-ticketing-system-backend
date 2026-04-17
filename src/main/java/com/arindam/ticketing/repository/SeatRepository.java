package com.arindam.ticketing.repository;

import com.arindam.ticketing.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByEventIdAndSeatNumberIn(Long eventId, List<Integer> seatNumbers);
    List<Seat> findByEventId(Long eventId);
}
