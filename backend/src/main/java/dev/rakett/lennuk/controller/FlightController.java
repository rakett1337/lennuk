package dev.rakett.lennuk.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import dev.rakett.lennuk.dto.FlightDto;
import dev.rakett.lennuk.dto.SeatInfoDto;
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
    public ResponseEntity<?> getSeatsForFlight(@PathVariable Long id) {
        List<SeatInfoDto> seats = flightService.getSeatsForFlight(id);

        if (seats.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(seats);
    }
}