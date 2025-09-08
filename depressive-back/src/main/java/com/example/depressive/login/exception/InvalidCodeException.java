package com.example.depressive.login.exception;


public class InvalidCodeException extends RuntimeException {
    public InvalidCodeException() {
        super("无效的找回码");
    }
}
