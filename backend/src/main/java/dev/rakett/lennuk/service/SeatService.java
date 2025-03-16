package dev.rakett.lennuk.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

import dev.rakett.lennuk.dto.SeatInfoDto;
import dev.rakett.lennuk.exception.BadRequestException;
import dev.rakett.lennuk.model.SeatPreference;
import dev.rakett.lennuk.util.SeatCreator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeatService {
    private final SeatCreator seatCreator;

    public List<SeatInfoDto> getSeatMap(Long flightId) {
        return seatCreator.createSeatsForFlight(flightId);
    }

    public List<SeatInfoDto> getSeatMapWithRecommendations(Long flightId, SeatPreference preferences) {
        if (preferences == null) {
            throw new BadRequestException("Seat preferences cannot be null");
        }
        if (preferences.getNumberOfSeats() <= 0 || preferences.getNumberOfSeats() > 2) {
            throw new BadRequestException("Number of seats must be greater than zero and equal or less than two");
        }

        List<SeatInfoDto> allSeats = getSeatMap(flightId);
        List<SeatInfoDto> availableSeats = allSeats.stream()
                .filter(seat -> !seat.isBooked())
                .collect(Collectors.toList());

        if (availableSeats.size() < preferences.getNumberOfSeats()) {
            throw new BadRequestException("Not enough available seats on this flight");
        }

        availableSeats.forEach(seat -> seat.setRecommendationScore(preferences.calculateScore(
                seat.isWindow(),
                seat.isExtraLegroom(),
                seat.isExitRow())));

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

        Set<String> recommendedSeatNumbers = recommendedSeats.stream()
                .map(SeatInfoDto::getSeatNumber)
                .collect(Collectors.toSet());

        allSeats.forEach(seat -> seat.setRecommended(recommendedSeatNumbers.contains(seat.getSeatNumber())));
        return allSeats;
    }

    private List<SeatInfoDto> findSeatsTogetherWithHighestScore(List<SeatInfoDto> availableSeats,
            SeatPreference preferences) {
        int numberOfSeats = preferences.getNumberOfSeats();
        Map<Integer, List<SeatInfoDto>> seatsByRow = availableSeats.stream()
                .collect(Collectors.groupingBy(seat -> Integer.parseInt(seat.getSeatNumber().replaceAll("[A-Z]", ""))));

        List<List<SeatInfoDto>> validGroups = new ArrayList<>();
        for (List<SeatInfoDto> rowSeats : seatsByRow.values()) {
            Map<Character, SeatInfoDto> seatsByLetter = rowSeats.stream()
                    .collect(Collectors.toMap(
                            seat -> seat.getSeatNumber().charAt(seat.getSeatNumber().length() - 1),
                            seat -> seat,
                            (s1, s2) -> s1));

            if (numberOfSeats <= 3) {
                checkConsecutiveSeatsInSection(seatsByLetter, 'A', numberOfSeats, validGroups);
                checkConsecutiveSeatsInSection(seatsByLetter, 'D', numberOfSeats, validGroups);
            }
        }

        return validGroups.stream()
                .max(Comparator.comparing(group -> group.stream().mapToInt(SeatInfoDto::getRecommendationScore).sum()))
                .orElse(new ArrayList<>());
    }

    private void checkConsecutiveSeatsInSection(Map<Character, SeatInfoDto> seatsByLetter, char start,
            int groupSize, List<List<SeatInfoDto>> validGroups) {
        List<SeatInfoDto> group = IntStream.range(0, groupSize)
                .mapToObj(i -> (char) (start + i))
                .map(seatsByLetter::get)
                .takeWhile(Objects::nonNull)
                .collect(Collectors.toList());

        if (group.size() == groupSize) {
            validGroups.add(group);
        }
    }
}