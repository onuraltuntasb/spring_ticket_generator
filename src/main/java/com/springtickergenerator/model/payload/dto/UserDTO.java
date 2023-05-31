package com.springtickergenerator.model.payload.dto;

import com.springtickergenerator.entity.User;
import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    private User.UserStatus status;
}
