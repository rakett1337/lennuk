package dev.rakett.lennuk.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import dev.rakett.lennuk.dto.FlightDto;

@Component
public class FlightCreator {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public List<FlightDto> createSampleFlights() {
        List<FlightDto> flights = new ArrayList<>();
        LocalDate now = LocalDate.now();

        flights.add(FlightDto.builder()
                .id(1L)
                .origin("LON")
                .destination("BUD")
                .departureDate(now.plusDays(7).format(DATE_FORMATTER))
                .returnDate(now.plusDays(14).format(DATE_FORMATTER))
                .basePrice(new BigDecimal("149.99"))
                .build());

        flights.add(FlightDto.builder()
                .id(2L)
                .origin("LON")
                .destination("CPH")
                .departureDate(now.plusDays(3).format(DATE_FORMATTER))
                .returnDate(now.plusDays(10).format(DATE_FORMATTER))
                .basePrice(new BigDecimal("129.50"))
                .build());

        flights.add(FlightDto.builder()
                .id(3L)
                .origin("LON")
                .destination("FCO")
                .departureDate(now.plusDays(5).format(DATE_FORMATTER))
                .returnDate(now.plusDays(12).format(DATE_FORMATTER))
                .basePrice(new BigDecimal("189.99"))
                .build());

        return flights;
    }
}