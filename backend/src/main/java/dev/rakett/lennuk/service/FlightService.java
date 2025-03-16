package dev.rakett.lennuk.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import dev.rakett.lennuk.dto.FlightDto;
import dev.rakett.lennuk.entity.Flight;
import dev.rakett.lennuk.exception.BadRequestException;
import dev.rakett.lennuk.repository.FlightRepository;
import dev.rakett.lennuk.util.FlightCreator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FlightService {

    private final FlightRepository flightRepository;
    private final AmadeusApiService amadeusApiService;
    private final SeatService seatService;
    private final FlightCreator flightCreator;

    public void initializeFlights() {
        if (flightRepository.count() == 0) {
            List<Flight> flights;
            try {
                // The api is quite limited on free tier, so we hardcode origin to LON for now
                flights = amadeusApiService.fetchFlightDestinations("LON");
                if (flights == null || flights.isEmpty()) {
                    flights = flightCreator.createSampleFlights();
                }
            } catch (Exception e) {
                flights = flightCreator.createSampleFlights();
            }
            flightRepository.saveAll(flights);
            seatService.initializeBookedSeats();
        }
    }

    @Transactional(readOnly = true)
    public List<FlightDto> getFlights() {
        return flightRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<Flight> getFlightById(Long id) {
        if (id == null) {
            throw new BadRequestException("Flight ID cannot be null");
        }
        return flightRepository.findByIdWithBookedSeats(id);
    }

    private FlightDto convertToDto(Flight flight) {
        FlightDto dto = new FlightDto();
        BeanUtils.copyProperties(flight, dto);
        return dto;
    }
}