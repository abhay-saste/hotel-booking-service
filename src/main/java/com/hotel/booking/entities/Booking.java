package com.hotel.booking.entities;

import com.hotel.booking.constants.BookingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Getter
@Setter
public abstract class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="booking_decorators", joinColumns=@JoinColumn(name="booking_id"))
    @Column(name="decorator_name")
    private Set<String> appliedDecorators;

    private double totalPrice;
    private String description;
}