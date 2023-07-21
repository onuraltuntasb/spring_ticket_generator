package com.springticketgenerator.service;


import com.springticketgenerator.entity.User;
import com.springticketgenerator.model.payload.request.ResetPasswordRequest;
import com.springticketgenerator.model.payload.request.TokenRefreshRequest;
import com.springticketgenerator.model.payload.request.UserRequest;
import com.springticketgenerator.model.payload.request.UserUpdateRequest;
import com.springticketgenerator.model.payload.response.TokenRefreshResponse;
import com.springticketgenerator.model.payload.response.UserAuthResponse;

public interface UserService {
    User registerUser(UserRequest userRequest, boolean authenticated);

    User loginUser(User user);

    UserAuthResponse setUserOtherParams(User user, boolean authenticated, String opType);

    TokenRefreshResponse getTokenRefreshResponse(TokenRefreshRequest request);

    User updateUser(UserUpdateRequest userUpdateRequest, String email);

    User getUserWithPassworResetToken(String token);

    Boolean resetPassword (ResetPasswordRequest resetPasswordRequest);

    void deleteUser(Long userId);

    void createPasswordResetTokenForUser(User user , String token);

}
