package com.o1blog._blog.exeption;

public class EmailAlreadyTakenException extends RuntimeException {
    public EmailAlreadyTakenException() {
        super("Email is already taken");
    }
}
