package com.hotel.booking.decorator;

import com.hotel.booking.entities.RoomBooking;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.Set;

@Component
public class DecoratorFactory {
    public BookingComponent applyDecorators(BookingComponent baseComponent, Set<String> decoratorNames, long nights) {
        BookingComponent decoratedComponent = baseComponent;
        if (decoratorNames == null) return decoratedComponent;
        for (String name : decoratorNames) {
            decoratedComponent = switch (name.toUpperCase()) {
                case "BREAKFAST" -> new BreakfastDecorator(decoratedComponent, nights);
                case "LATE_CHECKOUT" -> new LateCheckoutDecorator(decoratedComponent);
                default -> decoratedComponent;
            };
        }
        return decoratedComponent;
    }
}