package com.springtickergenerator.service;

import com.springtickergenerator.entity.Event;
import com.springtickergenerator.model.payload.request.EventRequest;

public interface EventService {
    Event updateEvent(EventRequest eventRequest, Long eventId);
    Event saveEvent(EventRequest eventRequest, Long eventId);
}
