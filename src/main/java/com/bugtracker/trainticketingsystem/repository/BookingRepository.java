package com.trainticket.repository;

import com.trainticket.model.Booking;
import com.trainticket.model.TrainSchedule;
import com.trainticket.model.User;
import java.util.*;

public class BookingRepository {
    private final Map<String, Booking> bookings = new HashMap<>();

    public void save(Booking booking) {
        bookings.put(booking.getId(), booking);
    }

    public Optional<Booking> findById(String id) {
        return Optional.ofNullable(bookings.get(id));
    }

    public List<Booking> findAll() {
        return new ArrayList<>(bookings.values());
    }

    public List<Booking> findByUser(User user) {
        return bookings.values().stream()
                .filter(b -> b.getUser().equals(user))
                .toList();
    }

    public List<Booking> findBySchedule(TrainSchedule schedule) {
        return bookings.values().stream()
                .filter(b -> b.getTickets().stream()
                        .anyMatch(t -> t.getSchedule().equals(schedule)))
                .toList();
    }

    public int countBookedSeats(TrainSchedule schedule) {
        return (int) bookings.values().stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED)
                .flatMap(b -> b.getTickets().stream())
                .filter(t -> t.getSchedule().equals(schedule))
                .count();
    }

    public Set<Integer> getBookedSeatNumbers(TrainSchedule schedule) {
        Set<Integer> bookedSeats = new HashSet<>();
        bookings.values().stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED)
                .flatMap(b -> b.getTickets().stream())
                .filter(t -> t.getSchedule().equals(schedule))
                .forEach(t -> bookedSeats.add(t.getSeatNumber()));
        return bookedSeats;
    }

    public void delete(String id) {
        bookings.remove(id);
    }
}
