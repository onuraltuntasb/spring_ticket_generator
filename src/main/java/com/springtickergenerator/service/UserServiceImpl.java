package com.springtickergenerator.service;

import com.springtickergenerator.entity.RefreshToken;
import com.springtickergenerator.entity.User;
import com.springtickergenerator.exception.ResourceNotFoundException;
import com.springtickergenerator.exception.TokenCustomException;
import com.springtickergenerator.model.payload.request.TokenRefreshRequest;
import com.springtickergenerator.model.payload.request.UserRequest;
import com.springtickergenerator.model.payload.response.TokenRefreshResponse;
import com.springtickergenerator.model.payload.response.UserAuthResponse;
import com.springtickergenerator.repository.RoleRepository;
import com.springtickergenerator.repository.UserRepository;
import com.springtickergenerator.security.JwtUtils;
import jakarta.transaction.Transactional;
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


    @Transactional
    @Override
    public User registerUser(UserRequest userRequest, boolean authenticated) {

        User user = new User();

        if(userRequest.getAdminCreationSecret()!=null && !userRequest.getAdminCreationSecret().isEmpty() &&
                userRequest.getAdminCreationSecret().equals("ydSR&R*07I")){
            user.setRoles(Arrays.asList(roleRepository.findByName("ROLE_ADMIN")));
        }else{
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

        if(refreshToken ==null){
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
                        "Refresh token is not in database!"));

    }

    @Override
    public User updateUser(User user, Long userId) {

        User rUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with this id:" + userId
                ));

            //update just these fields
            rUser.setName(user.getName());

            //TODO before user status update need to finish other related tasks
            //rUser.setStatus(user.getStatus());

        return userRepository.save(rUser);
    }

    @Override
    public void deleteUser(Long userId) {

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
