package dev.rakett.lennuk.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeatPreference {
    private boolean windowSeat;
    private boolean extraLegroom;
    private boolean exitRowProximity;
    private boolean seatsTogetherRequired;
    private int numberOfSeats;

    // Assumptions have been made as to the importantance of preferences
    // In a real project we might let user decide the importance of said preferences. 
    public static final int WINDOW_WEIGHT = 10;
    public static final int LEGROOM_WEIGHT = 15;
    public static final int EXIT_ROW_WEIGHT = 12;

    public int calculateScore(boolean isWindow, boolean hasExtraLegroom, boolean isExitRow) {
        int score = 0;
        if (windowSeat && isWindow)
            score += WINDOW_WEIGHT;
        if (extraLegroom && hasExtraLegroom)
            score += LEGROOM_WEIGHT;
        if (exitRowProximity && isExitRow)
            score += EXIT_ROW_WEIGHT;
        return score;
    }
}