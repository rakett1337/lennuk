package dev.rakett.lennuk.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import dev.rakett.lennuk.dto.AmadeusFlightDestinationResponseDto;
import dev.rakett.lennuk.dto.AmadeusOAuthResponseDto;
import dev.rakett.lennuk.dto.AmadeusFlightDestinationResponseDto.FlightDestinationData;
import dev.rakett.lennuk.entity.Flight;
import dev.rakett.lennuk.exception.ExternalServiceException;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for interacting with the Amadeus API to fetch flight information.
 * @see <a href="https://developers.amadeus.com/self-service/apis-docs">Amadeus Api Docs</a>
 */
@Service
@Slf4j
public class AmadeusApiService {
    @Value("${AMADEUS_API_KEY}")
    private String apiKey;

    @Value("${AMADEUS_API_SECRET}")
    private String clientSecret;

    @Value("${AMADEUS_API_BASE_URL}")
    private String apiUrl;

    private static final Random random = new Random();
    private static final int EXPIRY_BUFFER_SECONDS = 60;
    private final AtomicReference<String> cachedToken = new AtomicReference<>();
    private final AtomicReference<Instant> tokenExpiration = new AtomicReference<>();
    private final RestTemplate restTemplate;

    /**
    * Constructor for AmadeusApiService.
    */
    public AmadeusApiService() {
        this.restTemplate = new RestTemplate();
    }

    /**
    * Fetches flight destinations from the Amadeus API based on a given origin.
    * Retries up to 3 times in case of a "Primitive Timeout" error (code 141).
    *
    * @param origin The origin airport IATA code (e.g., "LGW") or city code (e.g., "LON").
    * @return A list of Flight objects representing flight destinations.
    * @throws ExternalServiceException If there is an error in communication or response processing.
    */
    public List<Flight> fetchFlightDestinations(String origin) {
        int maxRetries = 3;
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                String token = getAccessToken();
                if (token == null) {
                    throw new ExternalServiceException("Failed to obtain Amadeus API access token");
                }

                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(token);
                headers.setContentType(MediaType.APPLICATION_JSON);

                UriComponentsBuilder builder = UriComponentsBuilder
                        .fromUriString(apiUrl + "/v1/shopping/flight-destinations")
                        .queryParam("origin", origin);

                ResponseEntity<AmadeusFlightDestinationResponseDto> response = restTemplate.exchange(
                        builder.toUriString(),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        AmadeusFlightDestinationResponseDto.class);

                if (response.getBody() != null) {
                    return mapToFlightEntities(response.getBody());
                } else {
                    throw new ExternalServiceException("Received null response from Amadeus API");
                }

                // To deal with the occasional 500 from amadeus api: 
                // {"errors":[{"status":500,"code":141,"title":"SYSTEM ERROR HAS OCCURRED","detail":"Primitive Timeout"}]}
            } catch (HttpServerErrorException.InternalServerError e) {
                if (shouldRetry(e)) {
                    log.warn("Primitive Timeout detected, retrying... (attempt {}/{})", attempt + 1, maxRetries);
                    attempt++;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new ExternalServiceException("Thread interrupted while retrying Amadeus API request", ie);
                    }
                } else {
                    log.error("Non-retryable error occurred: {}", e.getMessage(), e);
                    throw new ExternalServiceException("Error communicating with Amadeus API", e);
                }
            } catch (RestClientException e) {
                log.error("Error fetching flight data from Amadeus API", e);
                throw new ExternalServiceException("Error communicating with Amadeus API", e);
            } catch (ExternalServiceException e) {
                log.error(e.getMessage(), e);
                throw e;
            } catch (Exception e) {
                log.error("Unexpected error fetching flight data", e);
                throw new ExternalServiceException("Unexpected error fetching flight data: " + e.getMessage(), e);
            }
        }
        throw new ExternalServiceException("Failed to fetch flight data after " + maxRetries + " attempts");
    }

    /**
    * Retrieves an access token for the Amadeus API.  It first checks for a cached,
    * non-expired token. If a valid cached token is not found, it requests a new
    * token from the Amadeus API, caches it, and then returns it.
    *
    * @return The Amadeus API access token
    * @throws ExternalServiceException If there is an error in communication or response processing.
    * @see <a href="https://developers.amadeus.com/self-service/apis-docs/guides/developer-guides/API-Keys/authorization/">Amadeus Authorization Guide</a>
    */
    public String getAccessToken() {
        if (cachedToken.get() != null &&
                tokenExpiration.get() != null &&
                tokenExpiration.get().isAfter(Instant.now().plusSeconds(EXPIRY_BUFFER_SECONDS))) {
            return cachedToken.get();
        }
        log.debug("Requesting new access token from {}/v1/security/oauth2/token", apiUrl);
        String requestBody = "grant_type=client_credentials&client_id=" + apiKey +
                "&client_secret=" + clientSecret;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<AmadeusOAuthResponseDto> response = restTemplate.exchange(
                    apiUrl + "/v1/security/oauth2/token",
                    HttpMethod.POST,
                    request,
                    AmadeusOAuthResponseDto.class);
            AmadeusOAuthResponseDto responseBody = response.getBody();
            if (responseBody != null) {
                log.debug("Token response: {}", responseBody);
                cachedToken.set(responseBody.getAccessToken());
                int expirySeconds = responseBody.getExpiresIn();
                log.debug("Access token value: {}", responseBody.getAccessToken());
                log.debug("Raw expiry seconds: {}", expirySeconds);
                tokenExpiration.set(Instant.now().plusSeconds(expirySeconds));
                log.debug("Successfully obtained new access token, expires in {} seconds", expirySeconds);
                return responseBody.getAccessToken();
            }
            throw new ExternalServiceException("Failed to obtain access token: response body is null");
        } catch (RestClientException e) {
            log.error("Error obtaining access token: {}", e.getMessage(), e);
            throw new ExternalServiceException("Error communicating with Amadeus authentication service", e);
        } catch (Exception e) {
            log.error("Error obtaining access token: {} - {}", e.getClass().getName(), e.getMessage(), e);
            throw new ExternalServiceException("Unexpected error obtaining access token", e);
        }
    }

    /**
    * Determines if the error should trigger a retry based on Amadeus API error response.
    *
    * @param e The HttpServerErrorException received.
    * @return true if the error is a "Primitive Timeout" (code 141), otherwise false.
    */
    private boolean shouldRetry(HttpServerErrorException e) {
        try {
            String responseBody = e.getResponseBodyAsString();
            return responseBody.contains("\"code\":141") && responseBody.contains("\"detail\":\"Primitive Timeout\"");
        } catch (Exception ex) {
            log.warn("Failed to parse error response: {}", ex.getMessage());
            return false;
        }
    }

    /**
    * Maps the response DTO from the Amadeus API to a list of Flight entities.
    *
    * @param responseDto The response object from the Amadeus API.
    * @return A list of Flight objects.
    */
    private List<Flight> mapToFlightEntities(AmadeusFlightDestinationResponseDto responseDto) {
        List<Flight> flights = new ArrayList<>();
        Map<String, AmadeusFlightDestinationResponseDto.Dictionaries.LocationData> locations = responseDto
                .getDictionaries().getLocations();
        for (FlightDestinationData flightData : responseDto.getData()) {
            Flight flight = new Flight();
            flight.setOrigin(flightData.getOrigin());
            flight.setDestination(flightData.getDestination());
            flight.setDepartureDate(flightData.getDepartureDate());
            flight.setReturnDate(flightData.getReturnDate());
            if (flightData.getPrice() != null && flightData.getPrice().getTotal() != null) {
                flight.setBasePrice(new BigDecimal(flightData.getPrice().getTotal()));
            } else {
                flight.setBasePrice(BigDecimal.ZERO);
            }
            if (locations.containsKey(flightData.getOrigin())) {
                AmadeusFlightDestinationResponseDto.Dictionaries.LocationData locationData = locations
                        .get(flightData.getOrigin());
                flight.setSubType(locationData.getSubType());
                flight.setOriginDetailedName(locationData.getDetailedName());
            }
            if (locations.containsKey(flightData.getDestination())) {
                AmadeusFlightDestinationResponseDto.Dictionaries.LocationData locationData = locations
                        .get(flightData.getDestination());
                flight.setDestinationDetailedName(locationData.getDetailedName());
            }
            flight.setRows(15 + random.nextInt(5));
            flight.setSeatsPerRow(6);
            flight.setSeatLayout("3-3");
            flights.add(flight);
        }
        return flights;
    }
}