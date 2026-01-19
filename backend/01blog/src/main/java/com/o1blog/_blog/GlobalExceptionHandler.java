package com.o1blog._blog;

// import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.o1blog._blog.exection.PostNotFoundException;
import com.o1blog._blog.exection.UsernameAlreadyTakenException;

@RestControllerAdvice
public class GlobalExceptionHandler {

        // ---------- fallback ----------
        public record ApiError(String message) {
        }

        @ExceptionHandler(UsernameAlreadyTakenException.class)
        public ResponseEntity<ApiError> handleUsername(UsernameAlreadyTakenException ex) {
                return ResponseEntity.status(409).body(new ApiError(ex.getMessage()));
        }

        @ExceptionHandler(PostNotFoundException.class)
        public ResponseEntity<?> handle(PostNotFoundException ex) {
                return ResponseEntity.status(404).body(new ApiError(ex.getMessage()));
        }

}