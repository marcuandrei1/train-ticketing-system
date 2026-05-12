package com.trainticket.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TrainSchedule {
    private final String id;
    private final Train train;
    private final Route route;
    private final LocalTime departureTime;
    private final LocalDate operatingDate;
    private int delayMinutes;

    public TrainSchedule(String id, Train train, Route route, LocalTime departureTime, LocalDate operatingDate) {
        this.id = id;
        this.train = train;
        this.route = route;
        this.departureTime = departureTime;
        this.operatingDate = operatingDate;
        this.delayMinutes = 0;
    }

    public String getId() {
        return id;
    }

    public Train getTrain() {
        return train;
    }

    public Route getRoute() {
        return route;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public LocalDate getOperatingDate() {
        return operatingDate;
    }

    public int getDelayMinutes() {
        return delayMinutes;
    }

    public void setDelayMinutes(int delayMinutes) {
        this.delayMinutes = delayMinutes;
    }

    public LocalDateTime getActualDepartureDateTime() {
        return LocalDateTime.of(operatingDate, departureTime).plusMinutes(delayMinutes);
    }

    public LocalDateTime getStationDepartureTime(Station station) {
        int stationIndex = route.getStationIndex(station);
        if (stationIndex == -1) {
            return null;
        }
        Route.StationStop stop = route.getStops().get(stationIndex);
        return LocalDateTime.of(operatingDate, departureTime)
                .plusMinutes(stop.departureMinutesFromStart())
                .plusMinutes(delayMinutes);
    }

    public LocalDateTime getStationArrivalTime(Station station) {
        int stationIndex = route.getStationIndex(station);
        if (stationIndex == -1) {
            return null;
        }
        Route.StationStop stop = route.getStops().get(stationIndex);
        return LocalDateTime.of(operatingDate, departureTime)
                .plusMinutes(stop.arrivalMinutesFromStart())
                .plusMinutes(delayMinutes);
    }

    @Override
    public String toString() {
        String delayInfo = delayMinutes > 0 ? String.format(" [DELAYED +%d min]", delayMinutes) : "";
        return String.format("Schedule %s: %s on %s departing %s%s",
                id, train.getName(), route.getName(), departureTime, delayInfo);
    }
}
