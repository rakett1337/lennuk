package dev.rakett.lennuk.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FlightDto {
    private Long id;
    private String origin;
    private String destination;
    private String departureDate;
    private String returnDate;
    private BigDecimal basePrice;
}