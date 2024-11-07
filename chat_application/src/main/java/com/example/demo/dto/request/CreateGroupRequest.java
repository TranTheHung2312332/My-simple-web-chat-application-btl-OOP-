package com.example.demo.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateGroupRequest {

    @Pattern(regexp = "^[a-zA-Z0-9_ ]*$", message = "Invalid characters")
    private String name;

    private List<Long> members;
    private String avatarUrl;

}
