package com.trainticket.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Booking {
    private final String id;
    private final User user;
    private final List<Ticket> tickets;
    private final LocalDateTime bookingTime;
    private BookingStatus status;

    public enum BookingStatus {
        CONFIRMED, CANCELLED, PENDING
    }

    public Booking(User user) {
        this.id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.user = user;
        this.tickets = new ArrayList<>();
        this.bookingTime = LocalDateTime.now();
        this.status = BookingStatus.CONFIRMED;
    }

    public String getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public List<Ticket> getTickets() {
        return Collections.unmodifiableList(tickets);
    }

    public void addTicket(Ticket ticket) {
        tickets.add(ticket);
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Booking ID: %s\n", id));
        sb.append(String.format("Customer: %s (%s)\n", user.getName(), user.getEmail()));
        sb.append(String.format("Booking Time: %s\n", bookingTime));
        sb.append(String.format("Status: %s\n", status));
        sb.append(String.format("Tickets (%d):\n", tickets.size()));
        for (Ticket ticket : tickets) {
            sb.append("  - ").append(ticket).append("\n");
        }
        return sb.toString();
    }
}
