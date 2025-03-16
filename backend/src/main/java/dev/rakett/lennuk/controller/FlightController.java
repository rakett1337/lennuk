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

/**
 * Controller for managing flight and seat-related operations.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;
    private final SeatService seatService;

    /**
    * Initializes flight data.
    */
    @PostConstruct
    public void initialize() {
        flightService.initializeFlights();
    }

    /**
    * Retrieves a list of available flights originating from the default location.
    *
    * @return A ResponseEntity containing a list of FlightDto objects.
    */
    @GetMapping("/flights")
    public ResponseEntity<List<FlightDto>> getFlights() {
        return ResponseEntity.ok(flightService.getFlights());
    }

    /**
    * Retrieves a seat map for a given flight with recommendations based on preferences.
    * If the flight is not found, a 404 Not Found response is returned.
    * If an invalid number of seats is requested (e.g., less than or equal to zero/more than two),
    * a 400 Bad Request exception is thrown.
    *
    * @param id                    The ID of the flight.
    * @param windowSeat            (Optional) Preference for a window seat.
    * @param extraLegroom          (Optional) Preference for extra legroom.
    * @param exitRowProximity      (Optional) Preference for proximity to an exit row.
    * @param numSeats              The number of seats required (default: 1, must be greater than zero).
    * @param seatsTogetherRequired Whether the seats need to be together (default: false).
    * @return A ResponseEntity containing a SeatMapResponseDto with seat recommendations.
    * @throws BadRequestException  If the number of requested seats is not greater than zero or more than two.
    * @throws ResourceNotFoundException If the specified flight is not found.
    */
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