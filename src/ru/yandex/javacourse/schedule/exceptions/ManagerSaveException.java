package ru.yandex.javacourse.schedule.exceptions;

public class ManagerSaveException extends RuntimeException {

    public ManagerSaveException(String message, Throwable cause) {
        super(message, cause);
    }
}