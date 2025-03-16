package dev.rakett.lennuk.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Component;
import dev.rakett.lennuk.entity.Flight;

@Component
public class FlightCreator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Random random = new Random();

    public List<Flight> createSampleFlights() {
        List<Flight> flights = new ArrayList<>();

        flights.add(createFlight("LCY", "BUD", 1, 7, 150, 250));
        flights.add(createFlight("LCY", "CPH", 2, 8, 70, 160));
        flights.add(createFlight("LHR", "DUB", 5, 12, 200, 400));
        flights.add(createFlight("LGW", "FCO", 1, 5, 100, 220));

        return flights;
    }

    private Flight createFlight(String origin, String destination, int departureOffset, int returnOffset, int minPrice,
            int maxPrice) {
        LocalDate now = LocalDate.now();
        LocalDate departureDate = now.plusDays(departureOffset);
        LocalDate returnDate = now.plusDays(returnOffset);

        BigDecimal price = BigDecimal.valueOf(minPrice + (maxPrice - minPrice) * random.nextDouble()).setScale(2,
                RoundingMode.HALF_UP);

        Flight flight = new Flight();
        flight.setOrigin(origin);
        flight.setDestination(destination);
        flight.setDepartureDate(departureDate.format(DATE_FORMATTER));
        flight.setReturnDate(returnDate.format(DATE_FORMATTER));
        flight.setBasePrice(price);
        flight.setSubType("AIRPORT");
        flight.setOriginDetailedName(getDetailedName(origin));
        flight.setDestinationDetailedName(getDetailedName(destination));
        flight.setRows(15 + random.nextInt(5));
        flight.setSeatsPerRow(6);
        flight.setSeatLayout("3-3");

        return flight;
    }

    private String getDetailedName(String code) {
        switch (code) {
            case "LGW":
                return "GATWICK";
            case "CPH":
                return "KASTRUP";
            case "DUB":
                return "DUBLIN INTERNATIONAL";
            case "LCY":
                return "CITY AIRPORT";
            case "BUD":
                return "LISZT FERENC INTL";
            case "LHR":
                return "HEATHROW";
            case "FCO":
                return "FIUMICINO";
            default:
                return "UNKNOWN AIRPORT";
        }
    }
}