package com.o1blog._blog;

import java.time.Instant;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

import com.o1blog._blog.exeption.PostNotFoundException;
import com.o1blog._blog.exeption.UserNotFoundException;
import com.o1blog._blog.exeption.UsernameAlreadyTakenException;

@RestControllerAdvice
public class GlobalExceptionHandler {

        public record ApiError(
                        int status,
                        String error,
                        String message,
                        String path,
                        Instant timestamp) {
        }

        private ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest req) {
                ApiError body = new ApiError(
                                status.value(),
                                status.getReasonPhrase(),
                                message,
                                req.getRequestURI(),
                                Instant.now());
                return ResponseEntity.status(status).body(body);
        }

        // IMPORTANT: catches "You cannot ban yourself" etc.
        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException ex, HttpServletRequest req) {
                HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
                String msg = ex.getReason() != null ? ex.getReason() : "Request failed";
                return build(status, msg, req);
        }

        // If you throw ErrorResponseException sometimes (Spring 6+)
        @ExceptionHandler(ErrorResponseException.class)
        public ResponseEntity<ApiError> handleErrorResponse(ErrorResponseException ex, HttpServletRequest req) {
                HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
                String msg = ex.getMessage() != null ? ex.getMessage() : "Request failed";
                return build(status, msg, req);
        }

        @ExceptionHandler(UsernameAlreadyTakenException.class)
        public ResponseEntity<ApiError> handleUsername(UsernameAlreadyTakenException ex, HttpServletRequest req) {
                return build(HttpStatus.CONFLICT, ex.getMessage(), req);
        }

        @ExceptionHandler(PostNotFoundException.class)
        public ResponseEntity<ApiError> handlePostNotFound(PostNotFoundException ex, HttpServletRequest req) {
                return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
        }

        @ExceptionHandler(UserNotFoundException.class)
        public ResponseEntity<ApiError> handleUserNotFound(UserNotFoundException ex, HttpServletRequest req) {
                return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
        }

        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public ResponseEntity<ApiError> handleMaxSize(MaxUploadSizeExceededException ex, HttpServletRequest req) {
                return build(HttpStatus.PAYLOAD_TOO_LARGE, "File too large (max request size exceeded).", req);
        }

        @ExceptionHandler(MultipartException.class)
        public ResponseEntity<ApiError> handleMultipart(MultipartException ex, HttpServletRequest req) {
                return build(HttpStatus.PAYLOAD_TOO_LARGE, "Upload failed (request too large or invalid multipart).",
                                req);
        }

        // last fallback (never expose internal details)
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiError> handleAny(Exception ex, HttpServletRequest req) {
                return build(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong.", req);
        }
}
