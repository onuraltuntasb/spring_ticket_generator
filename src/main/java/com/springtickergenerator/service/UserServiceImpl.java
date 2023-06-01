package com.springtickergenerator.service;

import com.springtickergenerator.entity.RefreshToken;
import com.springtickergenerator.entity.User;
import com.springtickergenerator.exception.ResourceNotFoundException;
import com.springtickergenerator.exception.TokenRefreshException;
import com.springtickergenerator.model.payload.request.TokenRefreshRequest;
import com.springtickergenerator.model.payload.response.TokenRefreshResponse;
import com.springtickergenerator.model.payload.response.UserAuthResponse;
import com.springtickergenerator.repository.RefreshTokenRepository;
import com.springtickergenerator.repository.RoleRepository;
import com.springtickergenerator.repository.UserRepository;
import com.springtickergenerator.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final RoleRepository roleRepository;


    @Override
    public User registerUser(User user, boolean authenticated) {

        user.setRoles(Arrays.asList(roleRepository.findByName("ROLE_USER")));

        user.setStatus(User.UserStatus.ACTIVE);

        String plainPassword = user.getPassword();
        if (plainPassword != null) {
            user.setPassword(passwordEncoder.encode(plainPassword));
        } else {
            throw new ResourceNotFoundException("User password not found!");
        }

        User rUser = null;

        rUser = userRepository.save(user);

        return rUser;
    }

    @Override
    public User loginUser(User user) {

        User rUser = (User) userRepository
                .findUserByEmail(user.getEmail())
                .orElseThrow(() ->
                        new ResourceNotFoundException
                                ("Not found email with id = " + user.getEmail()));

        if (!rUser.getName().equals(user.getName())) {
            //just throw bad credentials for security purposes
            log.error("username is wrong!");
            throw new BadCredentialsException("Bad credentials!");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(), user.getPassword()
                ));


        return rUser;
    }

    public UserAuthResponse setUserOtherParams(User user, boolean authenticated) {

        UserDetails userDetails = (UserDetails) userRepository
                .findUserByEmail(user.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Not found email with id = " + user.getEmail()));

        RefreshToken refreshToken = null;

        if (authenticated) {
            refreshToken = refreshTokenService
                    .createRefreshToken(user.getId());
        }
        try {
            UserAuthResponse userAuthResponse = new UserAuthResponse();
            userAuthResponse = UserAuthResponse.builder()
                    .name(user.getName())
                    .email(user.getEmail())
                    .authorities(user.getAuthorities())
                    .jwtToken(new JwtUtils().generateToken(userDetails))
                    .jwtRefreshToken(refreshToken.getToken())
                    .build();
            return userAuthResponse;
        } catch (NullPointerException e) {
            log.error("userDetails or user possibly null!");
            return null;
        }
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
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token is not in database!"));

    }

    @Override
    public User updateUser(User user, Long userId) {

        User rUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with this id:" + userId
                ));

        if (rUser != null) {

            //update just these fields
            rUser.setName(user.getName());

            //TODO before user status update need to finish other related tasks
            //rUser.setStatus(user.getStatus());
        }

        return userRepository.save(rUser);
    }

    @Override
    public void deleteUser(Long userId) {

        User rUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with this id:" + userId
                ));

        refreshTokenService.deleteByUserId(userId);

        //TODO before delete need to finish other related tasks
        //deleting user is generally bad practise, disable user account instead deleting user
        userRepository.deleteById(userId);

    }
}
