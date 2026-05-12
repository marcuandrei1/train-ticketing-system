package com.trainticket.repository;

import com.trainticket.model.Station;
import com.trainticket.model.Train;
import com.trainticket.model.TrainSchedule;
import java.time.LocalDate;
import java.util.*;

public class ScheduleRepository {
    private final Map<String, TrainSchedule> schedules = new HashMap<>();

    public void save(TrainSchedule schedule) {
        schedules.put(schedule.getId(), schedule);
    }

    public Optional<TrainSchedule> findById(String id) {
        return Optional.ofNullable(schedules.get(id));
    }

    public List<TrainSchedule> findAll() {
        return new ArrayList<>(schedules.values());
    }

    public List<TrainSchedule> findByDate(LocalDate date) {
        return schedules.values().stream()
                .filter(s -> s.getOperatingDate().equals(date))
                .toList();
    }

    public List<TrainSchedule> findByTrain(Train train) {
        return schedules.values().stream()
                .filter(s -> s.getTrain().equals(train))
                .toList();
    }

    public List<TrainSchedule> findByStationAndDate(Station station, LocalDate date) {
        return schedules.values().stream()
                .filter(s -> s.getOperatingDate().equals(date))
                .filter(s -> s.getRoute().containsStation(station))
                .toList();
    }

    public void delete(String id) {
        schedules.remove(id);
    }
}
