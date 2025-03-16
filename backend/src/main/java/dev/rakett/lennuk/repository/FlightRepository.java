package dev.rakett.lennuk.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dev.rakett.lennuk.entity.Flight;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

    @Query("SELECT DISTINCT f FROM Flight f LEFT JOIN FETCH f.bookedSeats WHERE f.origin = :origin OR " +
            "((:origin = 'LON' AND (f.origin = 'LGW' OR f.origin = 'LHR' OR f.origin = 'STN' OR f.origin = 'LCY' OR f.origin = 'LTN')))")
    List<Flight> findByOrigin(@Param("origin") String origin);

    @Query("SELECT f FROM Flight f LEFT JOIN FETCH f.bookedSeats WHERE f.id = :id")
    Optional<Flight> findByIdWithBookedSeats(@Param("id") Long id);
}
