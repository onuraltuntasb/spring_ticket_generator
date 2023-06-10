package com.springticketgenerator.controller;

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
    public ResponseEntity<?> saveTicket(@Valid @RequestBody TicketRequest ticketRequest
            , @RequestHeader(name = "Authorization") String token
            , @RequestParam(name = "event-id") Long eventId
    ) {

        if (token == null || eventId == null) {
            return ResponseEntity.badRequest().body("Bad request!");
        }

        String email = jwtUtils.extractUsername(token);


       userRepository.findUserByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("user not found with this email :" + email));

        return ResponseEntity.ok().body(ticketService.saveTicket(ticketRequest, eventId));
    }

    @PutMapping("/update")
    public ResponseEntity<?> saveTicket(@Valid @RequestBody TicketRequest ticketRequest
            , @RequestHeader(name = "Authorization") String token
            , @RequestParam(name = "event-id") Long eventId
            , @RequestParam(name = "ticket-id") Long ticketId
    ) {

        if (token == null || eventId == null || ticketId == null) {
            return ResponseEntity.badRequest().body("Bad request!");
        }

        String email = jwtUtils.extractUsername(token);


      userRepository.findUserByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("user not found with this email :" + email));

        return ResponseEntity.ok().body(ticketService.updateTicket(ticketRequest, eventId, ticketId));
    }

    //TODO after start date deleting ticket is real real concern

    @PutMapping("/delete")
    public ResponseEntity<?> deleteTicket(
            @RequestParam(name = "ticket-id") Long ticketId,
            @RequestParam(name = "event-id") Long eventId,
            @RequestHeader(name = "Authorization") String token
    ) {

        if (ticketId == null || eventId == null || token == null) {
            return ResponseEntity.badRequest().body("Bad request!");
        }

        String email = jwtUtils.extractUsername(token);

        userRepository.findUserByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("user not found with this email :" + email));

        ticketService.deleteTicket(ticketId, eventId);
        return ResponseEntity.ok().body("success");

    }


}
