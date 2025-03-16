package dev.rakett.lennuk.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import org.springframework.stereotype.Service;
import dev.rakett.lennuk.dto.FlightDto;
import dev.rakett.lennuk.dto.SeatInfoDto;

@Service
public class FlightService {
    public List<FlightDto> getFlights() {
        List<FlightDto> flights = new ArrayList<>();
        flights.add(FlightDto.builder()
                .id(1L)
                .origin("LON")
                .destination("BUD")
                .departureDate("2023-11-15")
                .returnDate("2023-11-22")
                .basePrice(new BigDecimal("149.99"))
                .build());

        flights.add(FlightDto.builder()
                .id(2L)
                .origin("LON")
                .destination("CPH")
                .departureDate("2023-11-16")
                .returnDate("2023-11-23")
                .basePrice(new BigDecimal("129.50"))
                .build());

        flights.add(FlightDto.builder()
                .id(3L)
                .origin("LON")
                .destination("FCO")
                .departureDate("2023-11-17")
                .returnDate("2023-11-24")
                .basePrice(new BigDecimal("189.99"))
                .build());

        return flights;
    }

    public List<SeatInfoDto> getSeatsForFlight(Long flightId) {
        List<SeatInfoDto> seats = new ArrayList<>();

        boolean flightExists = getFlights()
                .stream()
                .anyMatch(flight -> flight.getId().equals(flightId));

        if (!flightExists) {
            return seats;
        }

        for (int row = 1; row <= 6; row++) {
            for (char seatLetter = 'A'; seatLetter <= 'F'; seatLetter++) {
                String seatNumber = row + String.valueOf(seatLetter);

                boolean isBooked = Math.random() > 0.7;

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