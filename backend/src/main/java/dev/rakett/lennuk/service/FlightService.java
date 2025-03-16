package dev.rakett.lennuk.service;

import java.util.List;
import org.springframework.stereotype.Service;
import dev.rakett.lennuk.dto.FlightDto;
import dev.rakett.lennuk.dto.SeatInfoDto;
import dev.rakett.lennuk.exception.BadRequestException;
import dev.rakett.lennuk.exception.ResourceNotFoundException;
import dev.rakett.lennuk.model.SeatPreference;
import dev.rakett.lennuk.util.FlightCreator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FlightService {
    private final FlightCreator flightCreator;
    private final SeatService seatService;

    private List<FlightDto> cachedFlights;

    public List<FlightDto> getFlights() {
        if (cachedFlights == null) {
            cachedFlights = flightCreator.createSampleFlights();
        }
        return cachedFlights;
    }

    public List<SeatInfoDto> getSeatsForFlight(Long flightId) {
        validateFlightExists(flightId);
        return seatService.getSeatMap(flightId);
    }

    public List<SeatInfoDto> getSeatsWithRecommendations(Long flightId, SeatPreference preferences) {
        if (flightId == null) {
            throw new BadRequestException("Flight ID cannot be null");
        }
        validateFlightExists(flightId);
        return seatService.getSeatMapWithRecommendations(flightId, preferences);
    }

    private void validateFlightExists(Long flightId) {
        if (flightId == null) {
            throw new BadRequestException("Flight ID cannot be null");
        }
        boolean flightExists = getFlights().stream()
                .anyMatch(flight -> flight.getId().equals(flightId));
        if (!flightExists) {
            throw new ResourceNotFoundException("Flight", "id", flightId);
        }
    }
}