package dev.rakett.lennuk.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import dev.rakett.lennuk.dto.FlightDto;
import dev.rakett.lennuk.dto.SeatMapResponseDto;
import dev.rakett.lennuk.entity.Flight;
import dev.rakett.lennuk.exception.BadRequestException;
import dev.rakett.lennuk.exception.ResourceNotFoundException;
import dev.rakett.lennuk.model.SeatPreference;
import dev.rakett.lennuk.service.FlightService;
import dev.rakett.lennuk.service.SeatService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;
    private final SeatService seatService;

    @PostConstruct
    public void initialize() {
        flightService.initializeFlights();
    }

    @GetMapping("/flights")
    public ResponseEntity<List<FlightDto>> getFlights() {
        return ResponseEntity.ok(flightService.getFlights());
    }

    @GetMapping("/flights/{id}/seats")
    public ResponseEntity<SeatMapResponseDto> getSeatsForFlight(
            @PathVariable Long id,
            @RequestParam(required = false) Boolean windowSeat,
            @RequestParam(required = false) Boolean extraLegroom,
            @RequestParam(required = false) Boolean exitRowProximity,
            @RequestParam(defaultValue = "1") int numSeats,
            @RequestParam(defaultValue = "false") boolean seatsTogetherRequired) {
        if (numSeats <= 0 || numSeats > 2) {
            throw new BadRequestException("Number of seats must be greater than zero and less than or equal to two");
        }

        Flight flight = flightService.getFlightById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "ID", id));

        SeatPreference preferences = SeatPreference.builder()
                .windowSeat(Boolean.TRUE.equals(windowSeat))
                .extraLegroom(Boolean.TRUE.equals(extraLegroom))
                .exitRowProximity(Boolean.TRUE.equals(exitRowProximity))
                .numberOfSeats(numSeats)
                .seatsTogetherRequired(seatsTogetherRequired)
                .build();

        SeatMapResponseDto seats = seatService.getSeatMapWithRecommendations(flight, preferences);
        return ResponseEntity.ok(seats);
    }
}