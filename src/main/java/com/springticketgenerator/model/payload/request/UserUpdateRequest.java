package com.springticketgenerator.model.payload.request;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class UserUpdateRequest {
    private String name;
    private String email;
    private String oldPassword;
    private String password;
    private String confirmPassword;

}
