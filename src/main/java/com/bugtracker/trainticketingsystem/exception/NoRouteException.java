package com.trainticket.exception;

public class NoRouteException extends RuntimeException {
    public NoRouteException(String from, String to) {
        super(String.format("No route found between %s and %s", from, to));
    }
}
