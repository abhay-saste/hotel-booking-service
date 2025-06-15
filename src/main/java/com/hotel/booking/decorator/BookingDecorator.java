package com.hotel.booking.decorator;

abstract class BookingDecorator implements BookingComponent {
    protected BookingComponent wrapped;

    BookingDecorator(BookingComponent component) {
        this.wrapped = component;
    }

    public double getPrice() {
        return wrapped.getPrice();
    }

    public String getDescription() {
        return wrapped.getDescription();
    }
}