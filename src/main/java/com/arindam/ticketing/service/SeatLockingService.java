package com.arindam.ticketing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatLockingService {

    private final StringRedisTemplate redisTemplate;

    //Tries to lock a seat using Redis SET NX.
    //Returns true if lock was acquired, false if someone else already holds it.
    public boolean lockSeat(Long eventId, Integer seatNumber, String userName) {
        String key = "lock:event:" + eventId + ":seat:" + seatNumber;

        //5-minute Time-To-Live (TTL) so locks automatically expire if a user abandons checkout.
        Boolean lockAcquired = redisTemplate.opsForValue()
                .setIfAbsent(key, userName, Duration.ofMinutes(5));

        if (Boolean.TRUE.equals(lockAcquired)) {
            log.info("Redis Lock ACQUIRED for Seat {} by {}", seatNumber, userName);
            return true;
        } else {
            log.warn("Redis Lock DENIED for Seat {}. Already held by another user.", seatNumber);
            return false;
        }
    }


    //Releases the lock once the permanent MySQL transaction is complete.
    public void unlockSeat(Long eventId, Integer seatNumber) {
        String key = "lock:event:" + eventId + ":seat:" + seatNumber;
        redisTemplate.delete(key);
        log.info("Redis Lock RELEASED for Seat {}", seatNumber);
    }
}