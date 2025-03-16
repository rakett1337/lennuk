package dev.rakett.lennuk.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatInfo {
    private boolean recommended;
    private String seatNumber;
    private boolean isWindow;
    private boolean isAisle;
    private boolean isExitRow;
    private boolean isExtraLegroom;
    private boolean isBooked;
    private int recommendationScore;
}