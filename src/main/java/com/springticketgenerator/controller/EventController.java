package com.springticketgenerator.controller;

import com.springticketgenerator.entity.Event;
import com.springticketgenerator.entity.User;
import com.springticketgenerator.exception.ResourceNotFoundException;
import com.springticketgenerator.model.payload.request.EventRequest;
import com.springticketgenerator.repository.EventRepository;
import com.springticketgenerator.repository.UserRepository;
import com.springticketgenerator.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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


    @PostMapping("/save")
    public ResponseEntity<?> saveEvent(@Valid @RequestBody EventRequest eventRequest,
                                       @CookieValue(name = "eg-auth-cookie", defaultValue = "emptyOrNull")
                                       String egAuthCookie) {

        String email = egAuthCookie.substring(egAuthCookie.indexOf("email") + 6);

        User user = (User) userRepository.findUserByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User not found with this email :" + email));

        return ResponseEntity.ok().body(eventService.saveEvent(eventRequest, user.getId()));
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateEvent(@Valid @RequestBody EventRequest eventRequest,
                                         @CookieValue(name = "eg-auth-cookie", defaultValue = "emptyOrNull")
                                         String egAuthCookie, @RequestParam(value = "event-id") Long eventId) {

        String email = egAuthCookie.substring(egAuthCookie.indexOf("email") + 6);

        Event event1 = eventRepository.findById(eventId).orElseThrow(
                () -> new ResourceNotFoundException("event is not found with this id : " + eventId));

        if (event1.getUser().getEmail().equals(email)) {

            return ResponseEntity.ok().body(eventService.updateEvent(eventRequest, eventId));
        } else {
            return ResponseEntity.badRequest().body("You are not allowed to this action");
        }

    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteEvent(
            @CookieValue(name = "eg-auth-cookie", defaultValue = "emptyOrNull") String egAuthCookie,
            @RequestParam(value = "event-id") Long eventId) {
        String email = egAuthCookie.substring(egAuthCookie.indexOf("email") + 6);

        Event event1 = eventRepository.findById(eventId).orElseThrow(
                () -> new ResourceNotFoundException("event is not found with this id : " + eventId));

        if (event1.getUser().getEmail().equals(email)) {
            eventService.deleteEvent(eventId);
            return ResponseEntity.ok().body(true);
        } else {
            return ResponseEntity.badRequest().body("You are not allowed to this action");
        }

    }

    @GetMapping("/getallbyauthuser")
    public ResponseEntity<?> getAllEventsByAuthUser(
            @CookieValue(name = "eg-auth-cookie", defaultValue = "emptyOrNull") String egAuthCookie) {

        String email = egAuthCookie.substring(egAuthCookie.indexOf("email") + 6);

        User user = (User) userRepository.findUserByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User not found with this email :" + email));

        System.out.println("userId :" + user.getId());

        return ResponseEntity.ok().body(eventService.getAllEventsByUserId(user.getId()));
    }

    @GetMapping("/getall")
    public ResponseEntity<?> getAllEvents(
            @CookieValue(name = "eg-auth-cookie", defaultValue = "emptyOrNull") String egAuthCookie) {

        String email = egAuthCookie.substring(egAuthCookie.indexOf("email") + 6);

        return ResponseEntity.ok().body(eventService.getAllEvents());
    }

    @GetMapping("/get")
    public ResponseEntity<?> getEventByUserId(
            @CookieValue(name = "eg-auth-cookie", defaultValue = "emptyOrNull") String egAuthCookie,
            @RequestParam(value = "event-id") Long eventId) {

        return ResponseEntity.ok().body(eventService.getEventByUserId(eventId));
    }

}
