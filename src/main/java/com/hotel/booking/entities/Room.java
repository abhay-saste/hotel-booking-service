package com.hotel.booking.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
public class Room {
    public Room() {
    }

    public Room(Long id, String type, double price, int capacity, boolean available, Hotel hotel) {
        this.id = id;
        this.type = type;
        this.price = price;
        this.capacity = capacity;
        this.available = available;
        this.hotel = hotel;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private double price;
    private int capacity;
    private boolean available;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    @JsonBackReference
    private Hotel hotel;
}
