package com.springticketgenerator.service;

import com.springticketgenerator.entity.Event;
import com.springticketgenerator.model.payload.request.EventRequest;
import com.springticketgenerator.model.payload.response.EventResponse;

import java.util.List;

public interface EventService {
    EventResponse updateEvent(EventRequest eventRequest, Long eventId);

    EventResponse saveEvent(EventRequest eventRequest, Long eventId);

    void deleteEvent(Long eventId);

    List<EventResponse> getAllEventsByUserId(Long userId);

    List<EventResponse> getAllEvents();

    EventResponse getEventByUserId(Long userId);


}
