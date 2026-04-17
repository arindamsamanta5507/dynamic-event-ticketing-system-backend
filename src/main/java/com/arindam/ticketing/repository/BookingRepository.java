package com.arindam.ticketing.repository;

import com.arindam.ticketing.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}
