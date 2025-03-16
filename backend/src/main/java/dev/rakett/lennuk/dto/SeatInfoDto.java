package dev.rakett.lennuk.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SeatInfoDto {
    private String seatNumber;
    private boolean isWindow;
    private boolean isAisle;
    private boolean isExitRow;
    private boolean isBooked;
}