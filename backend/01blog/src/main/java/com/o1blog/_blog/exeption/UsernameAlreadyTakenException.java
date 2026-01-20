package com.o1blog._blog.exeption;

public class UsernameAlreadyTakenException extends RuntimeException {
    public UsernameAlreadyTakenException() {
        super("Username is already taken");
    }
}
