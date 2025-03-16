package dev.rakett.lennuk.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatMapResponseDto {
    private List<SeatInfoDto> data;
    private String errorMessage;

    public SeatMapResponseDto(List<SeatInfoDto> data) {
        this.data = data;
        this.errorMessage = null;
    }

    public SeatMapResponseDto(String errorMessage) {
        this.data = null;
        this.errorMessage = errorMessage;
    }
}