package dev.rakett.lennuk.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "booked_seats")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = { "id", "seatNumber" })
public class BookedSeat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "flight_id")
    private Flight flight;

    private String seatNumber;
}