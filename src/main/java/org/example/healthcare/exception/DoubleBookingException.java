package org.example.healthcare.exception;

public class DoubleBookingException extends RuntimeException {

    public DoubleBookingException(String message) {
        super(message);
    }
}