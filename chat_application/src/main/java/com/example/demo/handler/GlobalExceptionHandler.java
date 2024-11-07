package com.example.demo.handler;

import com.example.demo.dto.response.ApiResponse;
import com.nimbusds.jose.JOSEException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.text.ParseException;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {

    public ResponseEntity<?> normalizeException(Object error, HttpStatus httpStatus){
        var response = new ApiResponse(false, Instant.now(), null, error);
        return new ResponseEntity<>(response, httpStatus);
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<?> handleAppException(AppException e){
        return normalizeException(e.getMessage(), e.getHttpStatusResponse());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        List<FieldError> errors = e.getBindingResult().getFieldErrors();
        List<String> errorMessages = errors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return normalizeException(errorMessages, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(JOSEException.class)
    public ResponseEntity<?> handleJOSException(JOSEException e){
        return normalizeException("Invalid token", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ParseException.class)
    public ResponseEntity<?> handleParseException(ParseException e){
        return normalizeException("Failed to parse", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> handleNoSuchElementException(NoSuchElementException e){
        return normalizeException("No such element", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleOtherException(Exception e){
        return normalizeException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
