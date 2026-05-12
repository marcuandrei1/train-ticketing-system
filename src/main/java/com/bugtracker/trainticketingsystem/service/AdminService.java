package com.trainticket.service;

import com.trainticket.model.*;
import com.trainticket.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    private final RouteRepository routeRepository;
    private final TrainRepository trainRepository;
    private final ScheduleRepository scheduleRepository;
    private final BookingRepository bookingRepository;
    private final StationRepository stationRepository;
    private final EmailService emailService;

    public AdminService(RouteRepository routeRepository, TrainRepository trainRepository,
                        ScheduleRepository scheduleRepository, BookingRepository bookingRepository,
                        StationRepository stationRepository, EmailService emailService) {
        this.routeRepository = routeRepository;
        this.trainRepository = trainRepository;
        this.scheduleRepository = scheduleRepository;
        this.bookingRepository = bookingRepository;
        this.stationRepository = stationRepository;
        this.emailService = emailService;
    }

    // Route Management
    public Route addRoute(String id, String name, List<Route.StationStop> stops) {
        Route route = new Route(id, name);
        route.setStops(stops);
        routeRepository.save(route);
        logger.info("Added new route: {}", route.getName());
        return route;
    }

    public void removeRoute(String id) {
        routeRepository.delete(id);
        logger.info("Removed route: {}", id);
    }

    public Optional<Route> modifyRoute(String id, String newName, List<Route.StationStop> newStops) {
        return routeRepository.findById(id).map(route -> {
            if (newName != null) route.setName(newName);
            if (newStops != null) route.setStops(newStops);
            routeRepository.save(route);
            logger.info("Modified route: {}", route.getName());
            return route;
        });
    }

    public Optional<Route> addStationToRoute(String routeId, Station station,
                                             int arrivalMinutes, int departureMinutes) {
        return routeRepository.findById(routeId).map(route -> {
            route.addStop(station, arrivalMinutes, departureMinutes);
            routeRepository.save(route);
            logger.info("Added station {} to route {}", station.getName(), route.getName());
            return route;
        });
    }

    public Optional<Route> removeStationFromRoute(String routeId, Station station) {
        return routeRepository.findById(routeId).map(route -> {
            route.removeStop(station);
            routeRepository.save(route);
            logger.info("Removed station {} from route {}", station.getName(), route.getName());
            return route;
        });
    }

    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    // Train Management
    public Train addTrain(String id, String name, int totalSeats, Train.TrainType type) {
        Train train = new Train(id, name, totalSeats, type);
        trainRepository.save(train);
        logger.info("Added new train: {}", train);
        return train;
    }

    public void removeTrain(String id) {
        trainRepository.delete(id);
        logger.info("Removed train: {}", id);
    }

    public Optional<Train> modifyTrain(String id, String newName, Integer newTotalSeats, Train.TrainType newType) {
        return trainRepository.findById(id).map(train -> {
            if (newName != null) train.setName(newName);
            if (newTotalSeats != null) train.setTotalSeats(newTotalSeats);
            if (newType != null) train.setType(newType);
            trainRepository.save(train);
            logger.info("Modified train: {}", train);
            return train;
        });
    }

    public List<Train> getAllTrains() {
        return trainRepository.findAll();
    }

    // Station Management
    public Station addStation(String id, String name, String city) {
        Station station = new Station(id, name, city);
        stationRepository.save(station);
        logger.info("Added new station: {}", station);
        return station;
    }

    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }

    // Booking Management
    public List<Booking> getBookingsForTrain(String trainId) {
        return trainRepository.findById(trainId)
                .map(train -> scheduleRepository.findByTrain(train).stream()
                        .flatMap(schedule -> bookingRepository.findBySchedule(schedule).stream())
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    public List<Booking> getBookingsForSchedule(String scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .map(bookingRepository::findBySchedule)
                .orElse(List.of());
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    // Delay Management
    public void setTrainDelay(String scheduleId, int delayMinutes) {
        scheduleRepository.findById(scheduleId).ifPresent(schedule -> {
            int previousDelay = schedule.getDelayMinutes();
            schedule.setDelayMinutes(delayMinutes);
            scheduleRepository.save(schedule);

            logger.info("Set delay of {} minutes for schedule {} (was {} minutes)",
                    delayMinutes, scheduleId, previousDelay);

            // Notify affected customers
            if (delayMinutes > previousDelay) {
                notifyCustomersOfDelay(schedule);
            }
        });
    }

    private void notifyCustomersOfDelay(TrainSchedule schedule) {
        List<Booking> affectedBookings = bookingRepository.findBySchedule(schedule);
        Set<User> notifiedUsers = affectedBookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED)
                .map(Booking::getUser)
                .collect(Collectors.toSet());

        for (User user : notifiedUsers) {
            emailService.sendDelayNotification(user, schedule);
            logger.info("Sent delay notification to: {}", user.getEmail());
        }

        logger.info("Notified {} customers about delay on {}",
                notifiedUsers.size(), schedule.getTrain().getName());
    }

    public List<TrainSchedule> getDelayedSchedules() {
        return scheduleRepository.findAll().stream()
                .filter(s -> s.getDelayMinutes() > 0)
                .collect(Collectors.toList());
    }
}
