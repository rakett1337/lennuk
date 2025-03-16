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

/**
 * Service for handling seat management operations, including seat map generation, 
 * seat booking recommendations, and initialization of booked seats for demonstration purposes.
 */
@Service
@RequiredArgsConstructor
public class SeatService {
    private final FlightRepository flightRepository;
    private final SeatCreator seatCreator;

    private static final int DEFAULT_ROWS = 15;
    private static final int DEFAULT_SEATS_PER_ROW = 6;

    /**
    * Initializes booked seats for all flights in the database.
    * Clears any previously booked seats and randomly assigns new booked seats
    * for approximately 30% of the total available seats.
    */
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

    /**
    * Retrieves the seat map for a given flight, marking booked seats.
    * 
    * @param flight The flight for which the seat map is required.
    * @return A list of SeatInfo objects representing the seat map.
    * @throws BadRequestException If the flight is null.
    */
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

    /**
    * Retrieves the seat map for a given flight and provides seat recommendations
    * based on passenger preferences.
    * 
    * @param flight The flight for which seat recommendations are needed.
    * @param preferences The seat preference criteria.
    * @return A SeatMapResponseDto containing the full seat map with recommendations.
    * @throws BadRequestException If flight or preferences are null, or if there are not enough available seats.
    */
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

    /**
    * Validates the input for seat selection requests.
    * 
    * @param flight The flight to validate.
    * @param preferences The seat preference criteria.
    * @throws BadRequestException If flight or preferences are null, or if the number
    *                             of requested seats is invalid.
    */
    private void validateInput(Flight flight, SeatPreference preferences) {
        if (flight == null || preferences == null) {
            throw new BadRequestException("Flight and preferences cannot be null");
        }
        if (preferences.getNumberOfSeats() <= 0) {
            throw new BadRequestException("Number of seats must be greater than zero");
        }
    }

    /**
    * Generates a seat map for a given flight.
    * 
    * @param flight The flight for which the seat map is generated.
    * @return A list of SeatInfo objects representing the flight's seat layout.
    */
    private List<SeatInfo> generateSeatMap(Flight flight) {
        List<SeatInfo> seats = new ArrayList<>();
        int rows = flight.getRows() != null ? flight.getRows() : DEFAULT_ROWS;
        int seatsPerRow = flight.getSeatsPerRow() != null ? flight.getSeatsPerRow() : DEFAULT_SEATS_PER_ROW;

        for (int row = 1; row <= rows; row++) {
            for (int seatNum = 1; seatNum <= seatsPerRow; seatNum++) {
                SeatInfo seatInfo = new SeatInfo();
                seatInfo.setSeatNumber(String.valueOf(row) + (char) ('A' + seatNum - 1));

                seatInfo.setWindow(seatNum == 1 || seatNum == 6); // A and F
                seatInfo.setAisle(seatNum == 3 || seatNum == 4); // C and D

                seatInfo.setExitRow(row <= 2 || row >= rows - 1);

                seatInfo.setExtraLegroom(row == 1);

                seats.add(seatInfo);
            }
        }
        return seats;
    }

    /**
    * Finds the best available seats based on the given preferences.
    * If seats together are required, it attempts to find adjacent seats in a row.
    * Otherwise, it selects the highest-scoring individual seats.
    * 
    * @param availableSeats The list of available (unbooked) seats.
    * @param preferences The seat preference criteria.
    * @return A list of recommended SeatInfo objects.
    * @throws BadRequestException If the requested number of seats together cannot be found.
    */
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

    /**
    * Attempts to find adjacent seats in the same row for a group booking.
    * 
    * @param availableSeats The list of available (unbooked) seats.
    * @param preferences The seat preference criteria.
    * @return A list of adjacent seats matching the requested number.
    * @throws BadRequestException If the requested number of seats together cannot be found.
    */
    private List<SeatInfo> findSeatsTogether(List<SeatInfo> availableSeats, SeatPreference preferences) {
        // Group seats by row
        Map<Integer, List<SeatInfo>> seatsByRow = availableSeats.stream()
                .collect(Collectors.groupingBy(seat -> getRowNumber(seat.getSeatNumber())));

        for (List<SeatInfo> rowSeats : seatsByRow.values()) {
            // Sort by seat letter (A-F)
            rowSeats.sort(Comparator.comparing(seat -> seat.getSeatNumber().charAt(seat.getSeatNumber().length() - 1)));

            for (int i = 0; i <= rowSeats.size() - preferences.getNumberOfSeats(); i++) {
                List<SeatInfo> group = rowSeats.subList(i, i + preferences.getNumberOfSeats());

                // Ensure seats are actually adjacent
                if (areSeatsAdjacent(group)) {
                    return group;
                }
            }
        }
        throw new BadRequestException("Could not find requested number of seats together");
    }

    /**
     * Extracts the row number from a seat number (e.g., "10A" -> 10).
     */
    private int getRowNumber(String seatNumber) {
        return Integer.parseInt(seatNumber.replaceAll("[^0-9]", ""));
    }

    /**
     * Checks if the seats are actually adjacent in the same row.
     */
    private boolean areSeatsAdjacent(List<SeatInfo> seats) {
        for (int i = 1; i < seats.size(); i++) {
            char prevSeat = seats.get(i - 1).getSeatNumber().charAt(seats.get(i - 1).getSeatNumber().length() - 1);
            char currSeat = seats.get(i).getSeatNumber().charAt(seats.get(i).getSeatNumber().length() - 1);

            if (currSeat - prevSeat != 1) {
                return false;
            }
        }
        return true;
    }

    /**
    * Marks the recommended seats in the full seat map.
    * 
    * @param allSeats The complete list of seats.
    * @param recommendedSeats The list of seats that are recommended.
    */
    private void markRecommendedSeats(List<SeatInfo> allSeats, List<SeatInfo> recommendedSeats) {
        Set<String> recommendedSeatNumbers = recommendedSeats.stream()
                .map(SeatInfo::getSeatNumber)
                .collect(Collectors.toSet());
        allSeats.forEach(seat -> seat.setRecommended(recommendedSeatNumbers.contains(seat.getSeatNumber())));
    }
}