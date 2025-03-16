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

/**
 * Service for managing flight data, including fetching from Amadeus API,
 * handling fallback data, and providing access to flight information from the database.
 * 
 * This service interacts with the Amadeus API to fetch flight destinations and 
 * initializes flight data. It also provides methods to retrieve flight information 
 * from the database.
 */
@Service
@RequiredArgsConstructor
public class FlightService {

    private final FlightRepository flightRepository;
    private final AmadeusApiService amadeusApiService;
    private final SeatService seatService;
    private final FlightCreator flightCreator;

    /**
    * Initializes flights by fetching data from the Amadeus API. If the API request fails 
    * or returns an empty result (due to free-tier limitations), fallback sample flights 
    * are created. 
    * 
    * This method is executed only if no flights exist in the repository.
    */
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

    /**
    * Retrieves all flights stored in the database and converts them into DTOs.
    * 
    * @return A list of FlightDto objects representing available flights.
    */
    @Transactional(readOnly = true)
    public List<FlightDto> getFlights() {
        return flightRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
    * Retrieves a specific flight by its ID, including booked seat information.
    * 
    * @param id The unique identifier of the flight.
    * @return An Optional containing the Flight entity if found.
    * @throws BadRequestException If the flight ID is null.
    */
    @Transactional(readOnly = true)
    public Optional<Flight> getFlightById(Long id) {
        if (id == null) {
            throw new BadRequestException("Flight ID cannot be null");
        }
        return flightRepository.findByIdWithBookedSeats(id);
    }

    /**
    * Converts a Flight entity to a FlightDto object.
    * 
    * @param flight The Flight entity to convert.
    * @return A FlightDto representation of the flight.
    */
    private FlightDto convertToDto(Flight flight) {
        FlightDto dto = new FlightDto();
        BeanUtils.copyProperties(flight, dto);
        return dto;
    }
}