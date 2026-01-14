package com.o1blog._blog;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

        // ---------- fallback ----------
        @ExceptionHandler(Exception.class)
        public ResponseEntity<?> handleAny(Exception ex) {
                // don’t leak internal exception details to client
                return ResponseEntity.internalServerError().body("");
        }
}