package com.example.depressive.login.exception;

public class ExpiredCodeException extends RuntimeException {
    public ExpiredCodeException() {
        super("找回码已过期");
    }
}