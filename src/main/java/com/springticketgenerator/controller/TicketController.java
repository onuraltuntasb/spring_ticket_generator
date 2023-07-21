package com.springticketgenerator.controller;

import com.springticketgenerator.entity.User;
import com.springticketgenerator.exception.ResourceNotFoundException;
import com.springticketgenerator.model.payload.request.TicketRequest;
import com.springticketgenerator.repository.UserRepository;
import com.springticketgenerator.security.JwtUtils;
import com.springticketgenerator.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/ticket")
public class TicketController {

    private final TicketService ticketService;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;


    @PostMapping("/save")
    public ResponseEntity<?> saveTicket(@Valid @RequestBody TicketRequest ticketRequest,
                                        @CookieValue(name = "eg-auth-cookie", defaultValue = "emptyOrNull")
                                        String egAuthCookie, @RequestParam(value = "event-id") Long eventId) {


        String email = egAuthCookie.substring(egAuthCookie.indexOf("email") + 6);

        User user = (User) userRepository.findUserByEmail(email)
                                         .orElseThrow(() -> new ResourceNotFoundException(
                                                 "user not found with this email :" + email));

        System.out.println("userToString : " + user.getId());

        return ResponseEntity.ok().body(ticketService.saveTicket(ticketRequest, eventId, user));
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateTicket(@Valid @RequestBody TicketRequest ticketRequest,
                                          @CookieValue(name = "eg-auth-cookie", defaultValue = "emptyOrNull")
                                          String egAuthCookie, @RequestParam(name = "event-id") Long eventId,
                                          @RequestParam(name = "ticket-id") Long ticketId) {

        String email = egAuthCookie.substring(egAuthCookie.indexOf("email") + 6);


        userRepository.findUserByEmail(email)
                      .orElseThrow(() -> new ResourceNotFoundException("user not found with this email :" + email));

        return ResponseEntity.ok().body(ticketService.updateTicket(ticketRequest, eventId, ticketId));
    }

    //TODO after start date deleting ticket is real real concern

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteTicket(@RequestParam(name = "ticket-id") Long ticketId,
                                          @RequestParam(name = "event-id") Long eventId,
                                          @CookieValue(name = "eg-auth-cookie", defaultValue = "emptyOrNull")
                                          String egAuthCookie) {
        String email = egAuthCookie.substring(egAuthCookie.indexOf("email") + 6);


        userRepository.findUserByEmail(email)
                      .orElseThrow(() -> new ResourceNotFoundException("user not found with this email :" + email));

        ticketService.deleteTicket(ticketId, eventId);
        return ResponseEntity.ok().body(true);

    }

    @GetMapping("/getallbyauthuser")
    public ResponseEntity<?> getAll(
            @CookieValue(name = "eg-auth-cookie", defaultValue = "emptyOrNull") String egAuthCookie) {

        String email = egAuthCookie.substring(egAuthCookie.indexOf("email") + 6);

        User user = (User) userRepository.findUserByEmail(email)
                                         .orElseThrow(() -> new ResourceNotFoundException(
                                                 "user not found with this email :" + email));

        return ResponseEntity.ok().body(ticketService.findAllByUserId(user.getId()));
    }


}
