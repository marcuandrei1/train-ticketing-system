package com.trainticket.repository;

import com.trainticket.model.Train;
import java.util.*;

public class TrainRepository {
    private final Map<String, Train> trains = new HashMap<>();

    public void save(Train train) {
        trains.put(train.getId(), train);
    }

    public Optional<Train> findById(String id) {
        return Optional.ofNullable(trains.get(id));
    }

    public List<Train> findAll() {
        return new ArrayList<>(trains.values());
    }

    public void delete(String id) {
        trains.remove(id);
    }

    public boolean exists(String id) {
        return trains.containsKey(id);
    }
}
