package com.springticketgenerator.service;

import com.springticketgenerator.entity.Event;
import com.springticketgenerator.model.payload.request.EventRequest;

public interface EventService {
    Event updateEvent(EventRequest eventRequest, Long eventId);
    Event saveEvent(EventRequest eventRequest, Long eventId);
    void deleteEvent(Long eventId);


}
