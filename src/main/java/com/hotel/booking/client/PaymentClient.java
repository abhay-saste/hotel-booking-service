package com.hotel.booking.client;

import com.hotel.booking.dto.TransactionRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "hotel-payment-service", url = "http://localhost:8083/payments")
public interface PaymentClient {

    @PostMapping("/transaction")
    String processTransaction(@RequestBody TransactionRequest request);
}