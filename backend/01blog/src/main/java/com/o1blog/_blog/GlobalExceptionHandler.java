package com.o1blog._blog;

// import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import com.o1blog._blog.exeption.PostNotFoundException;
import com.o1blog._blog.exeption.UsernameAlreadyTakenException;

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

        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public ResponseEntity<ApiError> handleMaxSize(MaxUploadSizeExceededException ex) {
                return ResponseEntity.status(413).body(new ApiError("File too large (max request size exceeded)."));
        }

        @ExceptionHandler(MultipartException.class)
        public ResponseEntity<ApiError> handleMultipart(MultipartException ex) {
                // often wraps the size exception depending on server
                return ResponseEntity.status(413)
                                .body(new ApiError("Upload failed (request too large or invalid multipart)."));
        }

}