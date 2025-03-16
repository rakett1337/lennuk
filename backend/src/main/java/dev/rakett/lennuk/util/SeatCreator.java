package dev.rakett.lennuk.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.springframework.stereotype.Component;
import dev.rakett.lennuk.model.SeatInfo;

@Component
public class SeatCreator {
    private static final Random random = new Random();

    public List<SeatInfo> createSeatsForFlight(Long flightId, int rows, int seatsPerRow) {
        List<SeatInfo> seats = new ArrayList<>();
        for (int row = 1; row <= rows; row++) {
            for (char seatLetter = 'A'; seatLetter < 'A' + seatsPerRow; seatLetter++) {
                String seatNumber = row + String.valueOf(seatLetter);
                boolean isBooked = random.nextDouble() > 0.7; // 30% chance booked
                seats.add(SeatInfo.builder()
                        .seatNumber(seatNumber)
                        .isWindow(seatLetter == 'A' || seatLetter == (char) ('A' + seatsPerRow - 1))
                        .isAisle(seatLetter == 'C' || seatLetter == (char) ('A' + seatsPerRow - 2))
                        .isExitRow(row == 1 || row == rows)
                        .isExtraLegroom(row == 1) // First row has extra legroom
                        .isBooked(isBooked)
                        .recommended(false)
                        .recommendationScore(0)
                        .build());
            }
        }
        return seats;
    }
}