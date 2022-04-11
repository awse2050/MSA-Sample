package com.example.userservice.vo;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class RequestUser {

    @NotNull(message = "cant not null email")
    @Size(min = 2, message =  "email size,,,, 2")
    @Email
    private String email;

    @NotNull(message = "cant not null name")
    @Size(min = 2, message =  "name size,,,, 2")
    private String name;

    @NotNull(message = "cant not null email")
    @Size(min = 8, message =  "pw size,,,, 8")
    private String pwd;
}
