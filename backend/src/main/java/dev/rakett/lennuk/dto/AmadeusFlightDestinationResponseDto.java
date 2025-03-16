package dev.rakett.lennuk.dto;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AmadeusFlightDestinationResponseDto {
    private List<FlightDestinationData> data;
    private Dictionaries dictionaries;

    @Data
    public static class FlightDestinationData {
        private String type;
        private String origin;
        private String destination;
        private String departureDate;
        private String returnDate;
        private Price price;

        @Data
        public static class Price {
            private String total;
        }
    }

    @Data
    public static class Dictionaries {
        private Map<String, String> currencies;
        private Map<String, LocationData> locations;
        private Meta meta;

        @Data
        public static class LocationData {
            private String subType;
            private String detailedName;
        }

        @Data
        public static class Meta {
            private String currency;
        }
    }
}