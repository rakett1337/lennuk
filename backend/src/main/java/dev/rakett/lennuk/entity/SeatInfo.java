package dev.rakett.lennuk.entity;

import lombok.Data;

@Data
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
