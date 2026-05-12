package com.trainticket.service;

import com.trainticket.model.Train;
import com.trainticket.model.TrainSchedule;
import com.trainticket.repository.ScheduleRepository;
import com.trainticket.repository.TrainRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class TrainService {
    private static final Logger logger = LoggerFactory.getLogger(TrainService.class);

    private final TrainRepository trainRepository;
    private final ScheduleRepository scheduleRepository;

    public TrainService(TrainRepository trainRepository, ScheduleRepository scheduleRepository) {
        this.trainRepository = trainRepository;
        this.scheduleRepository = scheduleRepository;
    }

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

    public Optional<Train> getTrain(String id) {
        return trainRepository.findById(id);
    }

    public List<TrainSchedule> getSchedulesForTrain(Train train) {
        return scheduleRepository.findByTrain(train);
    }
}
