package com.springtickergenerator.model.payload.request;

import com.springtickergenerator.entity.Role;
import com.springtickergenerator.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class UserRequest {
    private Long id;
    private String name;
    private String email;
    private String password;
    private List<Role> roles;
    private User.UserStatus status;
    private String adminCreationSecret;

}
