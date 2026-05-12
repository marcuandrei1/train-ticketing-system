package com.trainticket.repository;

import com.trainticket.model.Station;
import java.util.*;

public class StationRepository {
    private final Map<String, Station> stations = new HashMap<>();

    public void save(Station station) {
        stations.put(station.getId(), station);
    }

    public Optional<Station> findById(String id) {
        return Optional.ofNullable(stations.get(id));
    }

    public Optional<Station> findByName(String name) {
        return stations.values().stream()
                .filter(s -> s.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public List<Station> findAll() {
        return new ArrayList<>(stations.values());
    }

    public void delete(String id) {
        stations.remove(id);
    }

    public boolean exists(String id) {
        return stations.containsKey(id);
    }
}
