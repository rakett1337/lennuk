package dev.rakett.lennuk.entity;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "flights")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "bookedSeats")
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String origin;
    private String destination;
    private String departureDate;
    private String returnDate;
    private String subType;
    private String originDetailedName;
    private String destinationDetailedName;
    private BigDecimal basePrice;
    private Integer rows;
    private Integer seatsPerRow;
    private String seatLayout;

    @JsonManagedReference
    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BookedSeat> bookedSeats = new HashSet<>();

    public void addBookedSeat(String seatNumber) {
        BookedSeat seat = new BookedSeat();
        seat.setSeatNumber(seatNumber);
        seat.setFlight(this);
        bookedSeats.add(seat);
    }

    public boolean isSeatBooked(String seatNumber) {
        return bookedSeats.stream()
                .anyMatch(seat -> seat.getSeatNumber().equals(seatNumber));
    }

    public void clearBookedSeats() {
        bookedSeats.clear();
    }
}