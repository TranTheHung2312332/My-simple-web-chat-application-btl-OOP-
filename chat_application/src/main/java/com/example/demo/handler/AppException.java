package com.example.demo.handler;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Setter
@Getter
public class AppException extends RuntimeException {

    private HttpStatus httpStatusResponse = HttpStatus.CONFLICT;

    public AppException(){
        super();
    }

    public AppException(String message){
        super(message);
    }

    public AppException(String message, HttpStatus httpStatusResponse){
        super(message);
        this.httpStatusResponse = httpStatusResponse;
    }

}
