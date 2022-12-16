package net.stepniak.morenomodels.serviceserverless.exceptions;

public class DataExpiredException extends RuntimeException {
    public DataExpiredException(String message) {
        super(message);
    }
}
