package com.softuni.statssvc.exception;

public class StatNotFoundException extends RuntimeException {

    public StatNotFoundException(String message) {
        super(message);
    }
}
