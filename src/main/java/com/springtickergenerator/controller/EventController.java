package com.springtickergenerator.controller;

import com.springtickergenerator.entity.Event;
import com.springtickergenerator.entity.User;
import com.springtickergenerator.exception.ResourceNotFoundException;
import com.springtickergenerator.model.payload.dto.EventDTO;
import com.springtickergenerator.model.payload.request.EventRequest;
import com.springtickergenerator.repository.EventRepository;
import com.springtickergenerator.repository.UserRepository;
import com.springtickergenerator.security.JwtUtils;
import com.springtickergenerator.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/event")
public class EventController {

    private final EventService eventService;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final ModelMapper modelMapper;


    @PostMapping("/save")
    public ResponseEntity<?> saveEvent(@Valid @RequestBody EventRequest eventRequest
            , @RequestHeader(name = "Authorization") String token) {


        String email = jwtUtils.extractUsername(token.substring(7));

        User user = (User) userRepository.findUserByEmail(email).orElseThrow(
                ()->new ResourceNotFoundException("User not found with this email :" + email));

        return ResponseEntity.ok().body(eventService.saveEvent(eventRequest, user.getId()));
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateEvent(@Valid @RequestBody EventRequest eventRequest, @RequestParam(value = "event-id") Long eventId
            , @RequestHeader(name = "Authorization") String token) {



        Event event1 = eventRepository.findById(eventId).orElseThrow(
                () -> new ResourceNotFoundException("event is not found with this id : " + eventId));

        if (event1.getUser().getEmail().equals(jwtUtils.extractUsername(token.substring(7)))) {
            EventDTO eventDTO = modelMapper.map(eventService.updateEvent(eventRequest, eventId), EventDTO.class);
            return ResponseEntity.ok().body(eventDTO);
        } else {
            return ResponseEntity.badRequest().body("You are not allowed to this action");
        }

    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteEvent(@RequestParam(value = "event-id") Long eventId, @RequestHeader(name = "Authorization") String token) {

        Event event1 = eventRepository.findById(eventId).orElseThrow(() -> new ResourceNotFoundException("Event is not found with this id : " + eventId));

        String auth = jwtUtils.getAuthorityClaim(token);

        if (event1.getUser().getEmail().equals(jwtUtils.extractUsername(token.substring(7)))) {
            eventService.deleteEvent(eventId);
            return ResponseEntity.ok().body("success");
        } else {
            return ResponseEntity.badRequest().body("You are not allowed to this action");
        }

    }


}
