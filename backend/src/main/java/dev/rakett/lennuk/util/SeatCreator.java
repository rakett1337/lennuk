package dev.rakett.lennuk.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.springframework.stereotype.Component;
import dev.rakett.lennuk.dto.SeatInfoDto;

@Component
public class SeatCreator {
    private static final Random random = new Random();

    public List<SeatInfoDto> createSeatsForFlight(Long flightId) {
        List<SeatInfoDto> seats = new ArrayList<>();

        for (int row = 1; row <= 6; row++) {
            for (char seatLetter = 'A'; seatLetter <= 'F'; seatLetter++) {
                String seatNumber = row + String.valueOf(seatLetter);

                // Randomly mark some seats as booked (30% chance)
                boolean isBooked = random.nextDouble() > 0.7;

                seats.add(SeatInfoDto.builder()
                        .seatNumber(seatNumber)
                        .isWindow(seatLetter == 'A' || seatLetter == 'F')
                        .isAisle(seatLetter == 'C' || seatLetter == 'D')
                        .isExitRow(row == 1 || row == 6)
                        .isBooked(isBooked)
                        .build());
            }
        }

        return seats;
    }
}