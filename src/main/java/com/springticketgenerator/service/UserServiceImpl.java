package com.springticketgenerator.service;

import com.springticketgenerator.entity.Event;
import com.springticketgenerator.entity.PasswordResetToken;
import com.springticketgenerator.entity.RefreshToken;
import com.springticketgenerator.entity.User;
import com.springticketgenerator.exception.ResourceNotFoundException;
import com.springticketgenerator.exception.TokenCustomException;
import com.springticketgenerator.model.payload.request.ResetPasswordRequest;
import com.springticketgenerator.model.payload.request.TokenRefreshRequest;
import com.springticketgenerator.model.payload.request.UserRequest;
import com.springticketgenerator.model.payload.request.UserUpdateRequest;
import com.springticketgenerator.model.payload.response.*;
import com.springticketgenerator.repository.PasswordResetTokenRepository;
import com.springticketgenerator.repository.RoleRepository;
import com.springticketgenerator.repository.UserRepository;
import com.springticketgenerator.security.JwtUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final RoleRepository roleRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JdbcTemplate jdbcTemplate;


    //TODO expiryDate if not working change method getTime plus

    @Override
    public void createPasswordResetTokenForUser(User user, String token) {
        PasswordResetToken myToken = PasswordResetToken
                .builder()
                .token(token)
                .user(user)
                .expiryDate(new Date(new Date().getTime() + (60 * 24)))
                .build();
        passwordResetTokenRepository.save(myToken);
    }

    @Transactional
    @Override
    public User registerUser(UserRequest userRequest, boolean authenticated) {

        User user = new User();

        if (userRequest.getAdminCreationSecret() != null && !userRequest.getAdminCreationSecret().isEmpty() &&
                userRequest.getAdminCreationSecret().equals("ydSR&R*07I")) {
            user.setRoles(Arrays.asList(roleRepository.findByName("ROLE_ADMIN")));
        } else {
            user.setRoles(Arrays.asList(roleRepository.findByName("ROLE_USER")));
        }


        String plainPassword = userRequest.getPassword();
        if (plainPassword != null) {
            user.setPassword(passwordEncoder.encode(plainPassword));
        } else {
            throw new ResourceNotFoundException("User password not found!");
        }

        user.setEmail(userRequest.getEmail());
        user.setName(userRequest.getName());
        user.setStatus(User.UserStatus.ACTIVE);


        return userRepository.save(user);
    }

    @Override
    public User loginUser(User user) {

        User rUser = (User) userRepository
                .findUserByEmail(user.getEmail())
                .orElseThrow(() ->
                                     new ResourceNotFoundException
                                             ("Not found email with id = " + user.getEmail()));


        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(), user.getPassword()
                ));


        return rUser;
    }

    public UserAuthResponse setUserOtherParams(User user, boolean authenticated, String opType) {

        UserDetails userDetails = (UserDetails) userRepository
                .findUserByEmail(user.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Not found email with id = " + user.getEmail()));

        RefreshToken refreshToken = null;

        if (authenticated) {
            if (opType.equals("register")) {
                refreshToken = refreshTokenService
                        .createRefreshToken(user.getId());
            } else if (opType.equals("login")) {
                refreshToken = user.getRefreshToken();
            } else {
                throw new RuntimeException("opType is wrong");
            }

        }

        if (refreshToken == null) {
            throw new NullPointerException("Refresh token is null");
        }

        UserAuthResponse userAuthResponse = new UserAuthResponse();
        userAuthResponse = UserAuthResponse.builder()
                                           .name(user.getName())
                                           .email(user.getEmail())
                                           .authorities(user.getAuthorities())
                                           .jwtToken(new JwtUtils().generateToken(userDetails))
                                           .jwtRefreshToken(refreshToken.getToken())
                                           .build();
        return userAuthResponse;

    }

    @Override
    public TokenRefreshResponse getTokenRefreshResponse(TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                                  .map(refreshTokenService::verifyExpiration)
                                  .map(RefreshToken::getUser)
                                  .map(user -> {
                                      String token = new JwtUtils().generateToken(user);
                                      return new TokenRefreshResponse(token, requestRefreshToken);
                                  })
                                  .orElseThrow(() -> new TokenCustomException(requestRefreshToken,
                                                                              "Refresh token is not in database!"
                                  ));

    }

    @Override
    public User updateUser(UserUpdateRequest userUpdateRequest, String email) {

        User rUser = (User) userRepository.findUserByEmail(email)
                                          .orElseThrow(() -> new ResourceNotFoundException(
                                                  "User not found with this email:" + email
                                          ));

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userUpdateRequest.getEmail(), userUpdateRequest.getOldPassword()
                ));


        String plainPassword = userUpdateRequest.getPassword();
        if (plainPassword != null) {
            rUser.setPassword(passwordEncoder.encode(plainPassword));
        } else {
            throw new ResourceNotFoundException("User password not found!");
        }


        //TODO before user status update need to finish other related tasks
        //rUser.setStatus(user.getStatus());

        return userRepository.save(rUser);
    }

    @Override
    public Boolean resetPassword(ResetPasswordRequest resetPasswordRequest) {

        User rUser = getUserWithPassworResetToken(resetPasswordRequest.getToken());

        if (resetPasswordRequest.getNewPassword().equals(resetPasswordRequest.getConfirmNewPassword())) {
            rUser.setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
        } else {
            throw new ResourceNotFoundException("Passwords are not matching!");
        }

        //TODO before user status update need to finish other related tasks
        //rUser.setStatus(user.getStatus());

        userRepository.save(rUser);

        return true;
    }

    @Override
    public User getUserWithPassworResetToken(String token) {

        List<ResetPasswordTokenResponse> passwordResetTokenList = null;


        try {
            passwordResetTokenList = jdbcTemplate.query("SELECT * FROM password_reset_token WHERE token = ? ",
                                                        (rs, rowNum) ->
                                                                new ResetPasswordTokenResponse(
                                                                        rs.getLong("id"),
                                                                        rs.getString("token"),
                                                                        rs.getLong("user_id"),
                                                                        rs.getTimestamp("expiry_date")
                                                                ), token
                                                       );
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }


        ResetPasswordTokenResponse passwordResetToken = passwordResetTokenList.get(0);

        System.out.println("passwordResetToken : " + passwordResetToken);

        if (passwordResetToken.getExpiryDate().after(new Date())) {
            throw new RuntimeException("Token is expired!");
        }

        User user =
                userRepository.findById(passwordResetToken.getUserId()).orElseThrow(() -> new ResourceNotFoundException(
                        "user " + "not found with this id :" +
                                passwordResetToken.getUserId()));


        return user;
    }

    @Override
    public void deleteUser(Long userId) {

        System.out.println("deleting user...");

        userRepository.findById(userId)
                      .orElseThrow(() -> new ResourceNotFoundException(
                              "User not found with this id:" + userId
                      ));

        refreshTokenService.deleteByUserId(userId);

        //TODO before delete need to finish other related tasks
        //deleting user is generally bad practise, disable user account instead deleting user
        userRepository.deleteById(userId);

    }
}
