package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse {

    private boolean success;
    private Instant time;
    private Object data;
    private Object error = null;

    public ApiResponse(Object data){
        this.data = data;
        this.success = true;
        this.time = Instant.now();
    }

}
