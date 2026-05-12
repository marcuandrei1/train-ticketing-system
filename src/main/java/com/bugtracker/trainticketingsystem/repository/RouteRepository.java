package com.trainticket.repository;

import com.trainticket.model.Route;
import com.trainticket.model.Station;
import java.util.*;

public class RouteRepository {
    private final Map<String, Route> routes = new HashMap<>();

    public void save(Route route) {
        routes.put(route.getId(), route);
    }

    public Optional<Route> findById(String id) {
        return Optional.ofNullable(routes.get(id));
    }

    public List<Route> findAll() {
        return new ArrayList<>(routes.values());
    }

    public List<Route> findByStation(Station station) {
        return routes.values().stream()
                .filter(r -> r.containsStation(station))
                .toList();
    }

    public void delete(String id) {
        routes.remove(id);
    }

    public boolean exists(String id) {
        return routes.containsKey(id);
    }
}
