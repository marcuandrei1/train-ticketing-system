package com.trainticket.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Route {
    private final String id;
    private String name;
    private List<StationStop> stops;

    public Route(String id, String name) {
        this.id = id;
        this.name = name;
        this.stops = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<StationStop> getStops() {
        return Collections.unmodifiableList(stops);
    }

    public void addStop(Station station, int arrivalMinutesFromStart, int departureMinutesFromStart) {
        stops.add(new StationStop(station, arrivalMinutesFromStart, departureMinutesFromStart));
    }

    public void removeStop(Station station) {
        stops.removeIf(stop -> stop.station().equals(station));
    }

    public void setStops(List<StationStop> stops) {
        this.stops = new ArrayList<>(stops);
    }

    public boolean containsStation(Station station) {
        return stops.stream().anyMatch(stop -> stop.station().equals(station));
    }

    public int getStationIndex(Station station) {
        for (int i = 0; i < stops.size(); i++) {
            if (stops.get(i).station().equals(station)) {
                return i;
            }
        }
        return -1;
    }

    public record StationStop(Station station, int arrivalMinutesFromStart, int departureMinutesFromStart) {
        @Override
        public String toString() {
            return String.format("%s (arr: +%dmin, dep: +%dmin)",
                    station.getName(), arrivalMinutesFromStart, departureMinutesFromStart);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Route: %s (%s)\n", name, id));
        sb.append("Stops:\n");
        for (int i = 0; i < stops.size(); i++) {
            sb.append(String.format("  %d. %s\n", i + 1, stops.get(i)));
        }
        return sb.toString();
    }
}
