package com.springtickergenerator.service;


import com.springtickergenerator.entity.User;
import com.springtickergenerator.model.payload.request.TokenRefreshRequest;
import com.springtickergenerator.model.payload.response.TokenRefreshResponse;
import com.springtickergenerator.model.payload.response.UserAuthResponse;

public interface UserService {
    User registerUser(User user, boolean authenticated);

    User loginUser(User user);

    UserAuthResponse setUserOtherParams(User user, boolean authenticated);

    TokenRefreshResponse getTokenRefreshResponse(TokenRefreshRequest request);

    User updateUser(User user, Long userId);

    void deleteUser(Long userId);

}
