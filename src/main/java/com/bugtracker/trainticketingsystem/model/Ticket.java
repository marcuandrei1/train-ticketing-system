package com.trainticket.model;

import java.util.UUID;

public class Ticket {
    private final String id;
    private final TrainSchedule schedule;
    private final Station fromStation;
    private final Station toStation;
    private final int seatNumber;
    private final double price;

    public Ticket(TrainSchedule schedule, Station fromStation, Station toStation, int seatNumber, double price) {
        this.id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.schedule = schedule;
        this.fromStation = fromStation;
        this.toStation = toStation;
        this.seatNumber = seatNumber;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public TrainSchedule getSchedule() {
        return schedule;
    }

    public Station getFromStation() {
        return fromStation;
    }

    public Station getToStation() {
        return toStation;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return String.format("Ticket %s: %s -> %s on %s, Seat %d, $%.2f",
                id, fromStation.getName(), toStation.getName(),
                schedule.getTrain().getName(), seatNumber, price);
    }
}
