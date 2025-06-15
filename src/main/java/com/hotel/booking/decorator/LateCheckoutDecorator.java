package com.hotel.booking.decorator;

class LateCheckoutDecorator extends BookingDecorator {
    private static final double LATE_CHECKOUT_PRICE = 50.00;

    public LateCheckoutDecorator(BookingComponent component) {
        super(component);
    }

    @Override
    public double getPrice() {
        return super.getPrice() + LATE_CHECKOUT_PRICE;
    }

    @Override
    public String getDescription() {
        return super.getDescription() + " + Late Checkout (2 PM)";
    }
}