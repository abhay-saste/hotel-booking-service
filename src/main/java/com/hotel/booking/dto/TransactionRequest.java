package com.hotel.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {
    private Long bookingId;
    private Long userId;
    private Double amount;
    private String type; // PAYMENT or REFUND
}
