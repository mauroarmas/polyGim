package com.gymplatform.service;
public class BusinessConflictException extends RuntimeException {
    public BusinessConflictException() { super("Conflicto de estado"); }
}
