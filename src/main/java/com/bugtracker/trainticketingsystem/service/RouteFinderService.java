package com.trainticket.service;

import com.trainticket.exception.NoRouteFoundException;
import com.trainticket.model.Route;
import com.trainticket.model.Station;
import com.trainticket.model.TrainSchedule;
import com.trainticket.repository.ScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class RouteFinderService {
    private static final Logger logger = LoggerFactory.getLogger(RouteFinderService.class);
    private static final int MIN_TRANSFER_MINUTES = 15;
    private static final int MAX_TRANSFERS = 2;

    private final ScheduleRepository scheduleRepository;

    public RouteFinderService(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    public List<Journey> findJourneys(Station from, Station to, LocalDate date) {
        List<Journey> journeys = new ArrayList<>();

        // Find direct journeys
        journeys.addAll(findDirectJourneys(from, to, date));

        // Find journeys with transfers
        journeys.addAll(findJourneysWithTransfers(from, to, date));

        if (journeys.isEmpty()) {
            throw new NoRouteFoundException(from.getName(), to.getName());
        }

        // Sort by departure time
        journeys.sort(Comparator.comparing(j -> j.legs().get(0).departureTime()));

        return journeys;
    }

    private List<Journey> findDirectJourneys(Station from, Station to, LocalDate date) {
        List<Journey> directJourneys = new ArrayList<>();
        List<TrainSchedule> schedules = scheduleRepository.findByDate(date);

        for (TrainSchedule schedule : schedules) {
            Route route = schedule.getRoute();
            int fromIndex = route.getStationIndex(from);
            int toIndex = route.getStationIndex(to);

            if (fromIndex != -1 && toIndex != -1 && fromIndex < toIndex) {
                LocalDateTime departure = schedule.getStationDepartureTime(from);
                LocalDateTime arrival = schedule.getStationArrivalTime(to);

                JourneyLeg leg = new JourneyLeg(schedule, from, to, departure, arrival);
                directJourneys.add(new Journey(List.of(leg)));

                logger.debug("Found direct journey: {} -> {} via {}",
                        from.getName(), to.getName(), schedule.getTrain().getName());
            }
        }

        return directJourneys;
    }

    private List<Journey> findJourneysWithTransfers(Station from, Station to, LocalDate date) {
        List<Journey> journeysWithTransfers = new ArrayList<>();
        List<TrainSchedule> schedules = scheduleRepository.findByDate(date);

        // Build a graph of possible connections
        Map<Station, List<ScheduleSegment>> departuresFrom = new HashMap<>();

        for (TrainSchedule schedule : schedules) {
            List<Route.StationStop> stops = schedule.getRoute().getStops();
            for (int i = 0; i < stops.size() - 1; i++) {
                Station station = stops.get(i).station();
                departuresFrom.computeIfAbsent(station, k -> new ArrayList<>())
                        .add(new ScheduleSegment(schedule, i));
            }
        }

        // BFS to find paths with transfers
        Queue<PathState> queue = new LinkedList<>();
        queue.add(new PathState(from, new ArrayList<>(), null));

        while (!queue.isEmpty()) {
            PathState current = queue.poll();

            if (current.legs().size() > MAX_TRANSFERS) {
                continue;
            }

            List<ScheduleSegment> departures = departuresFrom.getOrDefault(current.station(), List.of());

            for (ScheduleSegment segment : departures) {
                TrainSchedule schedule = segment.schedule();
                Route route = schedule.getRoute();
                List<Route.StationStop> stops = route.getStops();

                // Check if this segment connects to our destination or another station
                for (int i = segment.startIndex() + 1; i < stops.size(); i++) {
                    Station nextStation = stops.get(i).station();
                    LocalDateTime departure = schedule.getStationDepartureTime(current.station());
                    LocalDateTime arrival = schedule.getStationArrivalTime(nextStation);

                    // Check transfer time constraint
                    if (current.lastArrival() != null &&
                            departure.isBefore(current.lastArrival().plusMinutes(MIN_TRANSFER_MINUTES))) {
                        continue;
                    }

                    // Avoid revisiting stations
                    if (current.legs().stream().anyMatch(l ->
                            l.fromStation().equals(nextStation) || l.toStation().equals(nextStation))) {
                        continue;
                    }

                    JourneyLeg newLeg = new JourneyLeg(schedule, current.station(), nextStation, departure, arrival);
                    List<JourneyLeg> newLegs = new ArrayList<>(current.legs());
                    newLegs.add(newLeg);

                    if (nextStation.equals(to)) {
                        if (newLegs.size() > 1) { // Only add if it's actually a transfer journey
                            journeysWithTransfers.add(new Journey(newLegs));
                            logger.debug("Found journey with {} transfer(s): {} -> {}",
                                    newLegs.size() - 1, from.getName(), to.getName());
                        }
                    } else if (newLegs.size() <= MAX_TRANSFERS) {
                        queue.add(new PathState(nextStation, newLegs, arrival));
                    }

                    break; // Only consider the furthest reasonable stop on this train
                }
            }
        }

        return journeysWithTransfers;
    }

    public record Journey(List<JourneyLeg> legs) {
        public LocalDateTime getDepartureTime() {
            return legs.get(0).departureTime();
        }

        public LocalDateTime getArrivalTime() {
            return legs.get(legs.size() - 1).arrivalTime();
        }

        public int getTransferCount() {
            return legs.size() - 1;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Journey (%d transfer%s):\n",
                    getTransferCount(), getTransferCount() != 1 ? "s" : ""));
            for (int i = 0; i < legs.size(); i++) {
                JourneyLeg leg = legs.get(i);
                sb.append(String.format("  %d. %s\n", i + 1, leg));
                if (i < legs.size() - 1) {
                    JourneyLeg nextLeg = legs.get(i + 1);
                    long waitMinutes = java.time.Duration.between(
                            leg.arrivalTime(), nextLeg.departureTime()).toMinutes();
                    sb.append(String.format("     [Transfer at %s - %d min wait]\n",
                            leg.toStation().getName(), waitMinutes));
                }
            }
            sb.append(String.format("  Total: %s -> %s\n",
                    legs.get(0).departureTime(), legs.get(legs.size() - 1).arrivalTime()));
            return sb.toString();
        }
    }

    public record JourneyLeg(TrainSchedule schedule, Station fromStation, Station toStation,
                             LocalDateTime departureTime, LocalDateTime arrivalTime) {
        @Override
        public String toString() {
            String delayInfo = schedule.getDelayMinutes() > 0 ?
                    String.format(" [+%dmin delay]", schedule.getDelayMinutes()) : "";
            return String.format("%s: %s (%s) -> %s (%s)%s",
                    schedule.getTrain().getName(),
                    fromStation.getName(), departureTime.toLocalTime(),
                    toStation.getName(), arrivalTime.toLocalTime(),
                    delayInfo);
        }
    }

    private record ScheduleSegment(TrainSchedule schedule, int startIndex) {}

    private record PathState(Station station, List<JourneyLeg> legs, LocalDateTime lastArrival) {}
}
