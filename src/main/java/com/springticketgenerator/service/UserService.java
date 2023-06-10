package com.springticketgenerator.service;


import com.springticketgenerator.entity.User;
import com.springticketgenerator.model.payload.request.TokenRefreshRequest;
import com.springticketgenerator.model.payload.request.UserRequest;
import com.springticketgenerator.model.payload.response.TokenRefreshResponse;
import com.springticketgenerator.model.payload.response.UserAuthResponse;

public interface UserService {
    User registerUser(UserRequest userRequest, boolean authenticated);

    User loginUser(User user);

    UserAuthResponse setUserOtherParams(User user, boolean authenticated);

    TokenRefreshResponse getTokenRefreshResponse(TokenRefreshRequest request);

    User updateUser(User user, Long userId);

    void deleteUser(Long userId);

}
