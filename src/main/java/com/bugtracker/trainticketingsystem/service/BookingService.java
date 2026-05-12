package com.trainticket.service;

import com.trainticket.exception.OverbookingException;
import com.trainticket.model.*;
import com.trainticket.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BookingService {
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    private static final double BASE_PRICE_PER_STOP = 15.0;

    private final BookingRepository bookingRepository;
    private final EmailService emailService;

    public BookingService(BookingRepository bookingRepository, EmailService emailService) {
        this.bookingRepository = bookingRepository;
        this.emailService = emailService;
    }

    public Booking createBooking(User user, List<BookingRequest> requests) {
        Booking booking = new Booking(user);
        List<Ticket> tickets = new ArrayList<>();

        for (BookingRequest request : requests) {
            validateAvailability(request.schedule(), request.numberOfTickets());

            Set<Integer> bookedSeats = bookingRepository.getBookedSeatNumbers(request.schedule());
            int totalSeats = request.schedule().getTrain().getTotalSeats();

            for (int i = 0; i < request.numberOfTickets(); i++) {
                int seatNumber = findAvailableSeat(bookedSeats, totalSeats);
                bookedSeats.add(seatNumber);

                double price = calculatePrice(request.schedule(), request.fromStation(), request.toStation());
                Ticket ticket = new Ticket(request.schedule(), request.fromStation(),
                        request.toStation(), seatNumber, price);
                tickets.add(ticket);
                booking.addTicket(ticket);
            }
        }

        bookingRepository.save(booking);
        logger.info("Booking created: {} with {} tickets", booking.getId(), tickets.size());

        emailService.sendBookingConfirmation(booking);

        return booking;
    }

    public Booking bookSingleTicket(User user, TrainSchedule schedule, Station from, Station to) {
        return createBooking(user, List.of(new BookingRequest(schedule, from, to, 1)));
    }

    public Booking bookMultipleTickets(User user, TrainSchedule schedule, Station from, Station to, int count) {
        return createBooking(user, List.of(new BookingRequest(schedule, from, to, count)));
    }

    private void validateAvailability(TrainSchedule schedule, int requestedTickets) {
        int bookedSeats = bookingRepository.countBookedSeats(schedule);
        int totalSeats = schedule.getTrain().getTotalSeats();
        int availableSeats = totalSeats - bookedSeats;

        if (requestedTickets > availableSeats) {
            throw new OverbookingException(
                    String.format("Cannot book %d tickets. Only %d seats available on %s",
                            requestedTickets, availableSeats, schedule.getTrain().getName()));
        }
    }

    private int findAvailableSeat(Set<Integer> bookedSeats, int totalSeats) {
        for (int seat = 1; seat <= totalSeats; seat++) {
            if (!bookedSeats.contains(seat)) {
                return seat;
            }
        }
        throw new OverbookingException("No available seats found");
    }

    private double calculatePrice(TrainSchedule schedule, Station from, Station to) {
        int fromIndex = schedule.getRoute().getStationIndex(from);
        int toIndex = schedule.getRoute().getStationIndex(to);
        int stops = Math.abs(toIndex - fromIndex);

        double multiplier = switch (schedule.getTrain().getType()) {
            case HIGH_SPEED -> 2.0;
            case EXPRESS -> 1.5;
            case INTERCITY -> 1.2;
            case REGIONAL -> 1.0;
        };

        return stops * BASE_PRICE_PER_STOP * multiplier;
    }

    public int getAvailableSeats(TrainSchedule schedule) {
        int bookedSeats = bookingRepository.countBookedSeats(schedule);
        return schedule.getTrain().getTotalSeats() - bookedSeats;
    }

    public List<Booking> getBookingsForSchedule(TrainSchedule schedule) {
        return bookingRepository.findBySchedule(schedule);
    }

    public record BookingRequest(TrainSchedule schedule, Station fromStation, Station toStation, int numberOfTickets) {}
}
