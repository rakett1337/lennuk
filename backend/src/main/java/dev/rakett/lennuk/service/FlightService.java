package dev.rakett.lennuk.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import dev.rakett.lennuk.dto.FlightDto;
import dev.rakett.lennuk.dto.SeatInfoDto;
import dev.rakett.lennuk.exception.BadRequestException;
import dev.rakett.lennuk.exception.ResourceNotFoundException;
import dev.rakett.lennuk.model.SeatPreference;
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

    public List<SeatInfoDto> getSeatsWithRecommendations(Long flightId, SeatPreference preferences) {
        boolean flightExists = getFlights().stream()
                .anyMatch(flight -> flight.getId().equals(flightId));
        if (!flightExists) {
            throw new ResourceNotFoundException("Flight", "id", flightId);
        }

        List<SeatInfoDto> allSeats = seatCreator.createSeatsForFlight(flightId);

        // Filter available seats
        List<SeatInfoDto> availableSeats = allSeats.stream()
                .filter(seat -> !seat.isBooked())
                .collect(Collectors.toList());

        // Check if there are enough available seats
        if (availableSeats.size() < preferences.getNumberOfSeats()) {
            throw new BadRequestException("Not enough available seats on this flight");
        }

        // Calculate scores
        availableSeats.forEach(seat -> seat.setRecommendationScore(preferences.calculateScore(
                seat.isWindow(),
                seat.isExtraLegroom(),
                seat.isExitRow())));

        // Select top seats based on preferences
        List<SeatInfoDto> recommendedSeats;
        if (preferences.isSeatsTogetherRequired() && preferences.getNumberOfSeats() > 1) {
            recommendedSeats = findSeatsTogetherWithHighestScore(availableSeats, preferences);
            if (recommendedSeats.isEmpty()) {
                throw new BadRequestException("Could not find requested number of seats together");
            }
        } else {
            recommendedSeats = availableSeats.stream()
                    .sorted(Comparator.comparing(SeatInfoDto::getRecommendationScore).reversed())
                    .limit(preferences.getNumberOfSeats())
                    .collect(Collectors.toList());
        }

        // Mark recommended seats
        Set<String> recommendedSeatNumbers = recommendedSeats.stream()
                .map(SeatInfoDto::getSeatNumber)
                .collect(Collectors.toSet());

        allSeats.forEach(seat -> seat.setRecommended(recommendedSeatNumbers.contains(seat.getSeatNumber())));

        return allSeats;
    }

    // Helper method to find seats together with highest score
    private List<SeatInfoDto> findSeatsTogetherWithHighestScore(List<SeatInfoDto> availableSeats,
            SeatPreference preferences) {
        // Group seats by row
        Map<Integer, List<SeatInfoDto>> seatsByRow = availableSeats.stream()
                .collect(Collectors.groupingBy(seat -> Integer.parseInt(seat.getSeatNumber().substring(0, 1))));

        // Find consecutive seats in each row
        List<List<SeatInfoDto>> validGroups = new ArrayList<>();

        for (List<SeatInfoDto> rowSeats : seatsByRow.values()) {
            // Sort by seat letter
            rowSeats.sort(Comparator.comparing(SeatInfoDto::getSeatNumber));

            // Check for consecutive seats
            for (int i = 0; i <= rowSeats.size() - preferences.getNumberOfSeats(); i++) {
                List<SeatInfoDto> group = rowSeats.subList(i, i + preferences.getNumberOfSeats());
                validGroups.add(new ArrayList<>(group));
            }
        }

        // Return group with highest total score
        return validGroups.stream()
                .max(Comparator.comparing(group -> group.stream().mapToInt(SeatInfoDto::getRecommendationScore).sum()))
                .orElse(new ArrayList<>());
    }
}