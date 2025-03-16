package dev.rakett.lennuk.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;

import dev.rakett.lennuk.model.SeatInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatMapResponseDto {
    private List<SeatInfo> data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String errorMessage;

    public SeatMapResponseDto(List<SeatInfo> data) {
        this.data = data;
        this.errorMessage = null;
    }

    public SeatMapResponseDto(String errorMessage) {
        this.data = null;
        this.errorMessage = errorMessage;
    }
}