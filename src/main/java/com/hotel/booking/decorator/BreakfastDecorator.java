package com.hotel.booking.decorator;

class BreakfastDecorator extends BookingDecorator {
    private static final double BREAKFAST_PRICE_PER_DAY = 15.00;
    private final long nights;

    public BreakfastDecorator(BookingComponent component, long nights) {
        super(component);
        this.nights = nights > 0 ? nights : 1;
    }

    @Override
    public double getPrice() {
        return super.getPrice() + (BREAKFAST_PRICE_PER_DAY * nights);
    }

    @Override
    public String getDescription() {
        return super.getDescription() + " + Daily Breakfast";
    }
}