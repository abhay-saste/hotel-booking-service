package com.hotel.booking.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class BookingStrategyFactory {
    private final Map<String, BookingStrategy> strategyMap;

    @Autowired
    public BookingStrategyFactory(List<BookingStrategy> strategies) {
        strategyMap = strategies.stream().collect(Collectors.toMap(s -> s.getStrategyType().toUpperCase(), Function.identity()));
    }

    public BookingStrategy getStrategy(String type) {
        log.info("Strategy type: {}", type);
        BookingStrategy strategy = strategyMap.get(type.toUpperCase());
        if (strategy == null) { throw new IllegalArgumentException("Invalid booking type: " + type); }
        return strategy;
    }
}