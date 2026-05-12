package com.trainticket.exception;

public class TrainNotFoundException extends RuntimeException {
    public TrainNotFoundException(String trainId) {
        super(String.format("Train not found: %s", trainId));
    }
}
