package dev.rakett.lennuk.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import dev.rakett.lennuk.dto.FlightDto;
import dev.rakett.lennuk.dto.SeatInfoDto;
import dev.rakett.lennuk.dto.SeatMapResponseDto;
import dev.rakett.lennuk.exception.BadRequestException;
import dev.rakett.lennuk.model.SeatPreference;
import dev.rakett.lennuk.service.FlightService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    @GetMapping("/flights")
    public ResponseEntity<List<FlightDto>> getFlights() {
        return ResponseEntity.ok(flightService.getFlights());
    }

    @GetMapping("/flights/{id}/seats")
    public ResponseEntity<?> getSeatsForFlight(
            @PathVariable Long id,
            @RequestParam(required = false) Boolean windowSeat,
            @RequestParam(required = false) Boolean extraLegroom,
            @RequestParam(required = false) Boolean exitRowProximity,
            @RequestParam(defaultValue = "1") int numSeats,
            @RequestParam(defaultValue = "false") boolean seatsTogetherRequired) {

        // Validate input parameters
        if (numSeats <= 0) {
            throw new BadRequestException("Number of seats must be greater than zero");
        } else if (numSeats > 2) {
            // Limit number of seats to 2 for now
            throw new BadRequestException("Number of seats can't be over 2");
        }

        // Create preference object
        SeatPreference preferences = SeatPreference.builder()
                .windowSeat(Boolean.TRUE.equals(windowSeat))
                .extraLegroom(Boolean.TRUE.equals(extraLegroom))
                .exitRowProximity(Boolean.TRUE.equals(exitRowProximity))
                .numberOfSeats(numSeats)
                .seatsTogetherRequired(seatsTogetherRequired)
                .build();

        // Get seat recommendations 
        List<SeatInfoDto> seats = flightService.getSeatsWithRecommendations(id, preferences);
        return ResponseEntity.ok(new SeatMapResponseDto(seats));
    }
}