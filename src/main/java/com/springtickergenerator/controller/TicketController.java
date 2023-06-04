package com.springtickergenerator.controller;

import com.springtickergenerator.entity.User;
import com.springtickergenerator.exception.ResourceNotFoundException;
import com.springtickergenerator.model.payload.request.EventRequest;
import com.springtickergenerator.model.payload.request.TicketRequest;
import com.springtickergenerator.repository.TicketRepository;
import com.springtickergenerator.repository.UserRepository;
import com.springtickergenerator.security.JwtUtils;
import com.springtickergenerator.service.TicketService;
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
    private final TicketRepository ticketRepository;



    @PostMapping("/save")
    public ResponseEntity<?> saveTicket(@Valid @RequestBody TicketRequest ticketRequest
            ,@RequestHeader(name = "Authorization") String token
            ,@RequestParam(name = "event-id") Long eventId
    ) {

        if (token == null || eventId ==null) {
            return ResponseEntity.badRequest().body("Bad request!");
        }

        String email = jwtUtils.extractUsername(token);


        User user = (User) userRepository.findUserByEmail(email).orElseThrow(
                ()->new ResourceNotFoundException("user not found with this email :" + email));

        return ResponseEntity.ok().body(ticketService.saveTicket(ticketRequest,eventId));
    }

    @PutMapping("/update")
    public ResponseEntity<?> saveTicket(@Valid @RequestBody TicketRequest ticketRequest
            ,@RequestHeader(name = "Authorization") String token
            ,@RequestParam(name = "event-id") Long eventId
            ,@RequestParam(name = "ticket-id") Long ticketId
    ) {

        if (token == null || eventId == null || ticketId ==null) {
            return ResponseEntity.badRequest().body("Bad request!");
        }

        String email = jwtUtils.extractUsername(token);


        User user = (User) userRepository.findUserByEmail(email).orElseThrow(
                ()->new ResourceNotFoundException("user not found with this email :" + email));

        return ResponseEntity.ok().body(ticketService.updateTicket(ticketRequest,eventId,ticketId));
    }

    //TODO after start date deleting ticket is real real concern

    @PutMapping("/delete")
    public ResponseEntity<?> deleteTicket(
            @RequestParam(name = "ticket-id") Long ticketId
            ,@RequestHeader(name = "Authorization") String token
    ) {

        if (ticketId == null ||token == null) {
            return ResponseEntity.badRequest().body("Bad request!");
        }

        String email = jwtUtils.extractUsername(token);


        User user = (User) userRepository.findUserByEmail(email).orElseThrow(
                ()->new ResourceNotFoundException("user not found with this email :" + email));

        ticketRepository.deleteById(ticketId);
        return ResponseEntity.ok().body("success");

    }



}
