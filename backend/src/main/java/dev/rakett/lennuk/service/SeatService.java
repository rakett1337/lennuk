package dev.rakett.lennuk.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.rakett.lennuk.dto.SeatMapResponseDto;
import dev.rakett.lennuk.entity.BookedSeat;
import dev.rakett.lennuk.entity.Flight;
import dev.rakett.lennuk.exception.BadRequestException;
import dev.rakett.lennuk.model.SeatInfo;
import dev.rakett.lennuk.model.SeatPreference;
import dev.rakett.lennuk.repository.FlightRepository;
import dev.rakett.lennuk.util.SeatCreator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeatService {
    private final FlightRepository flightRepository;
    private final SeatCreator seatCreator;

    private static final int DEFAULT_ROWS = 15;
    private static final int DEFAULT_SEATS_PER_ROW = 6;

    @Transactional
    public void initializeBookedSeats() {
        List<Flight> flights = flightRepository.findAll();
        Random random = new Random();
        for (Flight flight : flights) {
            flight.clearBookedSeats();
            List<SeatInfo> allSeats = seatCreator.createSeatsForFlight(
                    flight.getId(),
                    flight.getRows() != null ? flight.getRows() : DEFAULT_ROWS,
                    flight.getSeatsPerRow() != null ? flight.getSeatsPerRow() : DEFAULT_SEATS_PER_ROW);
            int seatsToBook = (int) (allSeats.size() * 0.3);
            for (int i = 0; i < seatsToBook; i++) {
                int randomIndex = random.nextInt(allSeats.size());
                SeatInfo seat = allSeats.remove(randomIndex);
                flight.addBookedSeat(seat.getSeatNumber());
            }
            flightRepository.save(flight);
        }
    }

    @Transactional(readOnly = true)
    public List<SeatInfo> getSeatMap(Flight flight) {
        if (flight == null) {
            throw new BadRequestException("Flight cannot be null");
        }

        List<SeatInfo> allSeats = generateSeatMap(flight);
        Set<String> bookedSeatNumbers = flight.getBookedSeats().stream()
                .map(BookedSeat::getSeatNumber)
                .collect(Collectors.toSet());
        allSeats.forEach(seat -> seat.setBooked(bookedSeatNumbers.contains(seat.getSeatNumber())));
        return allSeats;
    }

    @Transactional(readOnly = true)
    public SeatMapResponseDto getSeatMapWithRecommendations(Flight flight, SeatPreference preferences) {
        // Validate input
        validateInput(flight, preferences);

        List<SeatInfo> allSeats = getSeatMap(flight);
        List<SeatInfo> availableSeats = allSeats.stream()
                .filter(seat -> !seat.isBooked())
                .collect(Collectors.toList());

        if (availableSeats.size() < preferences.getNumberOfSeats()) {
            throw new BadRequestException("Not enough available seats on this flight");
        }

        // Calculate scores
        availableSeats.forEach(seat -> seat.setRecommendationScore(
                preferences.calculateScore(seat.isWindow(), seat.isExtraLegroom(), seat.isExitRow())));

        // Find recommended seats
        List<SeatInfo> recommendedSeats = findRecommendedSeats(availableSeats, preferences);
        markRecommendedSeats(allSeats, recommendedSeats);

        return new SeatMapResponseDto(allSeats);
    }

    private void validateInput(Flight flight, SeatPreference preferences) {
        if (flight == null || preferences == null) {
            throw new BadRequestException("Flight and preferences cannot be null");
        }
        if (preferences.getNumberOfSeats() <= 0) {
            throw new BadRequestException("Number of seats must be greater than zero");
        }
    }

    private List<SeatInfo> generateSeatMap(Flight flight) {
        List<SeatInfo> seats = new ArrayList<>();
        int rows = flight.getRows() != null ? flight.getRows() : DEFAULT_ROWS;
        int seatsPerRow = flight.getSeatsPerRow() != null ? flight.getSeatsPerRow() : DEFAULT_SEATS_PER_ROW;
        for (int row = 1; row <= rows; row++) {
            for (int seatNum = 1; seatNum <= seatsPerRow; seatNum++) {
                SeatInfo seatInfo = new SeatInfo();
                seatInfo.setSeatNumber(String.valueOf(row) + (char) ('A' + seatNum - 1));
                seatInfo.setWindow(seatNum == 1 || seatNum == seatsPerRow);
                seatInfo.setAisle(seatNum == 2 || seatNum == seatsPerRow - 1);
                seatInfo.setExitRow(row <= 2 || row >= rows - 1);
                seatInfo.setExtraLegroom(row <= 1);
                seats.add(seatInfo);
            }
        }
        return seats;
    }

    private List<SeatInfo> findRecommendedSeats(List<SeatInfo> availableSeats, SeatPreference preferences) {
        if (preferences.isSeatsTogetherRequired()) {
            return findSeatsTogether(availableSeats, preferences);
        } else {
            return availableSeats.stream()
                    .sorted(Comparator.comparingInt(SeatInfo::getRecommendationScore).reversed())
                    .limit(preferences.getNumberOfSeats())
                    .collect(Collectors.toList());
        }
    }

    private List<SeatInfo> findSeatsTogether(List<SeatInfo> availableSeats, SeatPreference preferences) {
        Map<Integer, List<SeatInfo>> seatsByRow = availableSeats.stream()
                .collect(Collectors.groupingBy(seat -> Integer.parseInt(seat.getSeatNumber().replaceAll("[A-Z]", ""))));

        for (List<SeatInfo> rowSeats : seatsByRow.values()) {
            rowSeats.sort(Comparator.comparing(SeatInfo::getSeatNumber));
            for (int i = 0; i <= rowSeats.size() - preferences.getNumberOfSeats(); i++) {
                List<SeatInfo> group = rowSeats.subList(i, i + preferences.getNumberOfSeats());
                if (group.stream().allMatch(seat -> !seat.isBooked())) {
                    return group;
                }
            }
        }
        throw new BadRequestException("Could not find requested number of seats together");
    }

    private void markRecommendedSeats(List<SeatInfo> allSeats, List<SeatInfo> recommendedSeats) {
        Set<String> recommendedSeatNumbers = recommendedSeats.stream()
                .map(SeatInfo::getSeatNumber)
                .collect(Collectors.toSet());
        allSeats.forEach(seat -> seat.setRecommended(recommendedSeatNumbers.contains(seat.getSeatNumber())));
    }
}