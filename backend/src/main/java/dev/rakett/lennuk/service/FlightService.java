package dev.rakett.lennuk.service;

import java.util.List;
import org.springframework.stereotype.Service;
import dev.rakett.lennuk.dto.FlightDto;
import dev.rakett.lennuk.dto.SeatInfoDto;
import dev.rakett.lennuk.util.FlightCreator;
import dev.rakett.lennuk.util.SeatCreator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FlightService {
    private final FlightCreator flightCreator;
    private final SeatCreator seatCreator;

    private List<FlightDto> cachedFlights;

    public List<FlightDto> getFlights() {
        if (cachedFlights == null) {
            cachedFlights = flightCreator.createSampleFlights();
        }
        return cachedFlights;
    }

    public List<SeatInfoDto> getSeatsForFlight(Long flightId) {
        boolean flightExists = getFlights().stream()
                .anyMatch(flight -> flight.getId().equals(flightId));

        if (!flightExists) {
            return List.of();
        }

        return seatCreator.createSeatsForFlight(flightId);
    }
}