package com.springticketgenerator.controller;

import com.springticketgenerator.entity.User;
import com.springticketgenerator.exception.ResourceNotFoundException;
import com.springticketgenerator.model.payload.dto.UserDTO;
import com.springticketgenerator.model.payload.request.*;
import com.springticketgenerator.model.payload.response.EmailGenericResponse;
import com.springticketgenerator.model.payload.response.UserAuthResponse;
import com.springticketgenerator.repository.UserRepository;
import com.springticketgenerator.security.JwtUtils;
import com.springticketgenerator.service.EmailServiceImpl;
import com.springticketgenerator.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final EmailServiceImpl emailService;


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRequest userRequest) {


        UserAuthResponse userAuthResponse = userService.setUserOtherParams(userService.registerUser(userRequest, true),
                                                                           true, "register"
                                                                          );
        userAuthResponse.setIsAuth(true);
        String auth = userAuthResponse.getJwtToken(); auth = auth + "email=" + userAuthResponse.getEmail();

        //TODO https secure and domain fix for prod

        ResponseCookie cookie = ResponseCookie
                .from("eg-auth-cookie", auth)
                .secure(false)
                .httpOnly(true)
                .maxAge(86400)
                .sameSite("Strict")
                .domain("localhost")
                .path("/")
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(userAuthResponse);


    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody User user) {

        UserAuthResponse userAuthResponse = userService.setUserOtherParams(userService.loginUser(user), true, "login");

        String auth = userAuthResponse.getJwtToken(); auth = auth + "email=" + userAuthResponse.getEmail();
        userAuthResponse.setIsAuth(true);
        ResponseCookie cookie = ResponseCookie
                .from("eg-auth-cookie", auth)
                .secure(false)
                .httpOnly(true)
                .maxAge(86400)
                .domain("localhost")
                .sameSite("Strict")
                .path("/")
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(userAuthResponse);

    }

    @GetMapping("/logout")
    public ResponseEntity<Boolean> logout() {

        ResponseCookie cookie = ResponseCookie
                .from("eg-auth-cookie", null)
                .secure(false)
                .httpOnly(true)
                .maxAge(0)
                .domain("localhost")
                .sameSite("Strict")
                .path("/")
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(true);
    }



    @PostMapping("/forgot-password")
    public void forgotPassword(@Valid @RequestBody UserRequest userRequest,HttpServletRequest request) {
        User user = (User) userRepository.findUserByEmail(userRequest.getEmail()).orElseThrow(
                () -> new ResourceNotFoundException("user is not found with this email : " + userRequest.getEmail()));

        String token = UUID.randomUUID().toString();
        userService.createPasswordResetTokenForUser(user, token);

        String requestURI = request.getRequestURI();
        String homeURL = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();

        try {
            emailService.sendmail(userRequest.getEmail(),"reset-password",homeURL+"/reset-password/"+token);
        } catch (MessagingException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    @PostMapping("/reset-password")
    public Boolean resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest,
                             HttpServletRequest request) {


      return userService.resetPassword(resetPasswordRequest);

    }



    @GetMapping("/check")
    public ResponseEntity<?> checkAuth(
            @CookieValue(name = "eg-auth-cookie", defaultValue = "emptyOrNull") String egAuthCookie) {

        String email = egAuthCookie.substring(egAuthCookie.indexOf("email") + 6);


        String token = egAuthCookie.substring(0, egAuthCookie.indexOf("email"));

        User user = (User) userRepository.findUserByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("user is not found with this email : " + email));

        UserAuthResponse userAuthResponse = UserAuthResponse
                .builder()
                .name(user.getName())
                .email(user.getEmail())
                .isAuth(jwtUtils.isTokenValid(token, email))
                .build();

        return ResponseEntity.ok(userAuthResponse);
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(userService.getTokenRefreshResponse(request));
    }

    @GetMapping("/hello")
    public ResponseEntity<?> getHello(HttpServletRequest request) {
        String cookie = request.getHeader(HttpHeaders.COOKIE);

        try {
            emailService.sendmail("onuraltuntas50@gmail.com", "deneme", "denedik geldi mi");
        } catch (MessagingException | IOException e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.ok("hello from ec2!");
    }


    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserUpdateRequest userUpdateRequest,
                                        HttpServletRequest req) {

        String egAuthCookie = req.getHeader(HttpHeaders.COOKIE); String email = egAuthCookie.substring(
                egAuthCookie.indexOf("email") + 6);

        User rUser = (User) userRepository.findUserByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User not found with this email:" + email));

        UserDTO userDTO = modelMapper.map(userService.updateUser(userUpdateRequest, rUser.getEmail()), UserDTO.class);
        return ResponseEntity.ok(userDTO);

    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(@RequestParam(value = "id", required = true) Long id,
                                        @RequestHeader(name = "Authorization") String token) {

        if (id == null) {
            return ResponseEntity.badRequest().body("Bad request!");
        }

        User user1 = userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("post is not found with this id : " + id));

        String auth = jwtUtils.getAuthorityClaim(token);

        if (user1.getEmail().equals(jwtUtils.extractUsername(token.substring(7))) || auth.equals("ROLE_ADMIN")) {
            userService.deleteUser(id); return ResponseEntity.ok("success");
        } else {
            return ResponseEntity.badRequest().body("You are not allowed to this action!");
        }

    }

}


