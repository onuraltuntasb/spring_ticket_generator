package com.springticketgenerator.model.payload.dto;

import com.springticketgenerator.entity.User;
import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    private User.UserStatus status;
}
