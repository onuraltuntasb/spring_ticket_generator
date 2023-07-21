package com.springticketgenerator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springticketgenerator.entity.Event;
import com.springticketgenerator.entity.Tag;
import com.springticketgenerator.entity.User;
import com.springticketgenerator.exception.ResourceNotFoundException;
import com.springticketgenerator.model.payload.request.EventRequest;
import com.springticketgenerator.model.payload.response.EventResponse;
import com.springticketgenerator.repository.EventRepository;
import com.springticketgenerator.repository.TagRepository;
import com.springticketgenerator.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;


import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    //TODO jdbcTemplate RuntimeException logs

    private final EventRepository eventRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    ZoneOffset zoneOffSet = ZoneOffset.of("+00:00");


    @Transactional
    @Override
    public EventResponse updateEvent(EventRequest eventRequest, Long eventId) {

        Event rEvent = eventRepository.findById(eventId)
                                      .orElseThrow(() -> new ResourceNotFoundException(
                                              "Event not found with this id : " + eventId));


        OffsetDateTime offsetCurrDateTime = OffsetDateTime.now(zoneOffSet);


        if (eventRequest.getStatus() != null && rEvent.getStatus() != null) {

            if (eventRequest.getStartDate() != null && eventRequest.getEndDate() != null &&
                    eventRequest.getTicketSellingStartDate() != null) {

                if (eventRequest.getStartDate().isAfter(offsetCurrDateTime)) {

                    if (eventRequest.getStartDate().isBefore(eventRequest.getEndDate())) {

                        if (eventRequest.getTicketSellingStartDate()
                                        .isBefore(eventRequest.getStartDate().minusMinutes(5))) {


                            rEvent.setStartDate(eventRequest.getStartDate());
                            rEvent.setEndDate(eventRequest.getEndDate());
                            rEvent.setTicketSellingStartDate(eventRequest.getTicketSellingStartDate());


                            if (eventRequest.getSeats() != null) {
                                try {
                                    rEvent.setSeats(objectMapper.writeValueAsString(eventRequest.getSeats()));
                                    rEvent.setSeatCount(eventRequest.getSeats().size());
                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                }
                            }


                        } else {
                            throw new RuntimeException("Current date must be 5min before ticket selling date");
                        }

                    } else {
                        throw new RuntimeException("Start date is after endDate");
                    }

                } else {
                    throw new RuntimeException("Start date is after currentDate");
                }

                rEvent.setUpdatedAt(offsetCurrDateTime);
                rEvent.setName(eventRequest.getName());
                rEvent.setDescription(eventRequest.getDescription());
                rEvent.setStatus(eventRequest.getStatus());
                rEvent.setSeatingImageReference(eventRequest.getSeatingImageReference());


            } else {
                throw new RuntimeException("Start date or endDate or ticket selling date is null");
            }


        } else {
            throw new RuntimeException("Event request status or event status is null");
        }


        Set<Long> requestTags = eventRequest.getTags();
        Set<Tag> tags = new HashSet<>();

        if (requestTags != null) {
            for (Long requestTag : requestTags) {

                tags.add(tagRepository.findById(requestTag).orElseThrow(
                        () -> new ResourceNotFoundException("Tag not found with this id : " + requestTag)));

            }
            rEvent.setTags(tags);
        }


        EventResponse eventResponse = null;
        try {
            eventResponse = EventResponse.builder().id(rEvent.getId()).name(rEvent.getName())
                                         .description(rEvent.getDescription()).seatCount(rEvent.getSeatCount())
                                         .startDate(rEvent.getStartDate()).endDate(rEvent.getEndDate())
                                         .ticketSellingStartDate(rEvent.getTicketSellingStartDate()).createdAt(
                            rEvent.getCreatedAt())
                                         .updatedAt(rEvent.getUpdatedAt()).status(rEvent.getStatus())
                                         .tags(new HashSet<>(rEvent.getTags().stream().map(el -> el.getId()).toList()))
                                         .seats(objectMapper.readValue(rEvent.getSeats(),
                                                                       new TypeReference<List<HashMap<String, Object>>>() {
                                                                       }
                                                                      )).seatingImageReference(
                            rEvent.getSeatingImageReference()).build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        eventRepository.save(rEvent);

        return eventResponse;

    }


    @Transactional
    @Override
    public EventResponse saveEvent(EventRequest eventRequest, Long userId) {

        Set<Long> requestTagIds = eventRequest.getTags();
        Set<Tag> tags = new HashSet<>();

        if (requestTagIds != null) {
            for (Long requestTagId : requestTagIds) {
                tags.add(tagRepository.findById(requestTagId).orElseThrow(
                        () -> new ResourceNotFoundException("Tag not found with this id :" + requestTagId)));

            }
        }


        Event event = null;
        OffsetDateTime offsetCurrDateTime = OffsetDateTime.now(zoneOffSet);


        if (eventRequest.getStartDate() != null && eventRequest.getEndDate() != null &&
                eventRequest.getTicketSellingStartDate() != null) {

            //event startDate is after current time
            if (eventRequest.getStartDate().isAfter(offsetCurrDateTime)) {

                //event startDate is before endDate
                if (eventRequest.getStartDate().isBefore(eventRequest.getEndDate())) {

                    //ticketSelling before startDate
                    if (eventRequest.getTicketSellingStartDate()
                                    .isBefore(eventRequest.getStartDate().minusMinutes(5))) {


                        event = Event.builder().name(eventRequest.getName()).description(eventRequest.getDescription())
                                     .seatCount(eventRequest.getSeats().size()).startDate(eventRequest.getStartDate())
                                     .ticketSellingStartDate(eventRequest.getTicketSellingStartDate())
                                     .endDate(eventRequest.getEndDate()).createdAt(offsetCurrDateTime)
                                     .updatedAt(offsetCurrDateTime).status(eventRequest.getStatus()).tags(tags)
                                     .seatingImageReference(eventRequest.getSeatingImageReference()).build();


                    } else {
                        throw new RuntimeException("Ticket selling start date must be after start date!");
                    }

                } else if (eventRequest.getStartDate().isAfter(eventRequest.getEndDate())) {
                    throw new RuntimeException("Start date is after end date");
                } else {
                    throw new RuntimeException("Start date is equal to end date");
                }

            } else {
                throw new RuntimeException("Start date is equal to now");
            }

        } else {
            throw new RuntimeException("Start date or endDate or ticket selling date is null");
        }


        User user = userRepository.findById(userId)
                                  .orElseThrow(() -> new ResourceNotFoundException(
                                          "User not found with this id : " + userId));

        try {
            event.setSeats(objectMapper.writeValueAsString(eventRequest.getSeats()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        user.getEvents().add(event);

        Event event1 = eventRepository.saveAndFlush(event);
        EventResponse eventResponse = null;
        try {
            eventResponse = EventResponse.builder().id(event1.getId()).name(event1.getName())
                                         .description(event1.getDescription()).seatCount(event1.getSeatCount())
                                         .startDate(event1.getStartDate()).ticketSellingStartDate(
                            event1.getTicketSellingStartDate())
                                         .endDate(event1.getEndDate()).createdAt(event1.getCreatedAt()).updatedAt(
                            event1.getUpdatedAt())
                                         .status(event1.getStatus())
                                         .seats(objectMapper.readValue(event.getSeats(),
                                                                       new TypeReference<List<HashMap<String, Object>>>() {
                                                                       }
                                                                      )).tags(
                            new HashSet<>(event1.getTags().stream().map(el -> el.getId()).toList()))
                                         .seatingImageReference(event1.getSeatingImageReference()).build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return eventResponse;
    }

    //TODO delete tickets after a certain time, store procedure
    @Override
    public void deleteEvent(Long eventId) {

        Event event = eventRepository.findById(eventId)
                                     .orElseThrow(() -> new ResourceNotFoundException("event not found with this id!"));

        if (!event.getStatus()) {
            eventRepository.deleteById(eventId);
        } else {
            throw new RuntimeException("Event status is active, must be disable");
        }
    }

    @Override
    public List<EventResponse> getAllEventsByUserId(Long userId) {

        List<Event> eventList = null;
        List<EventResponse> eventResponseList = new ArrayList<>();
        List<Long> tagList = null;

        EventResponse eventResponse = null;

        try {
            eventList = jdbcTemplate.query("SELECT * FROM event WHERE user_id = ? ",
                                           new BeanPropertyRowMapper<Event>(Event.class), userId
                                          );
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        for (Event event : eventList) {

            try {
                tagList = jdbcTemplate.queryForList("SELECT tag_id FROM event_tag WHERE event_id = ? ", Long.class,
                                                    event.getId()
                                                   );
            } catch (DataAccessException e) {
                throw new RuntimeException(e);
            }


            try {
                eventResponse = EventResponse.builder().id(event.getId()).name(event.getName())
                                             .description(event.getDescription()).seatCount(event.getSeatCount())
                                             .startDate(event.getStartDate()).ticketSellingStartDate(
                                event.getTicketSellingStartDate())
                                             .endDate(event.getEndDate()).createdAt(event.getCreatedAt()).updatedAt(
                                event.getUpdatedAt())
                                             .status(event.getStatus()).seats(objectMapper.readValue(event.getSeats(),
                                                                                                     new TypeReference<List<HashMap<String, Object>>>() {
                                                                                                     }
                                                                                                    )).tags(
                                new HashSet<>(tagList)).seatingImageReference(event.getSeatingImageReference())
                                             .build();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            eventResponseList.add(eventResponse);

        }

        return eventResponseList;

    }

    @Override
    public List<EventResponse> getAllEvents() {


        List<Event> eventList = null;
        List<EventResponse> eventResponseList = new ArrayList<>();
        List<Long> tagList = null;

        EventResponse eventResponse = null;

        try {
            eventList = jdbcTemplate.query("SELECT * FROM event", new BeanPropertyRowMapper<Event>(Event.class));
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        for (Event event : eventList) {

            try {
                tagList = jdbcTemplate.queryForList("SELECT tag_id FROM event_tag WHERE event_id = ? ", Long.class,
                                                    event.getId()
                                                   );
            } catch (DataAccessException e) {
                throw new RuntimeException(e);
            }


            try {
                eventResponse = EventResponse.builder().id(event.getId()).name(event.getName())
                                             .description(event.getDescription()).seatCount(event.getSeatCount())
                                             .startDate(event.getStartDate()).ticketSellingStartDate(
                                event.getTicketSellingStartDate())
                                             .endDate(event.getEndDate()).createdAt(event.getCreatedAt()).updatedAt(
                                event.getUpdatedAt())
                                             .status(event.getStatus()).seats(objectMapper.readValue(event.getSeats(),
                                                                                                     new TypeReference<List<HashMap<String, Object>>>() {
                                                                                                     }
                                                                                                    )).tags(
                                new HashSet<>(tagList)).seatingImageReference(event.getSeatingImageReference())
                                             .build();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            eventResponseList.add(eventResponse);

        }

        return eventResponseList;

    }

    @Override
    public EventResponse getEventByUserId(Long userId) {

        Event event = null;
        EventResponse eventResponse = null;
        List<Long> tagList = null;
        try {
            event = jdbcTemplate.queryForObject("SELECT * FROM event WHERE id=?",
                                                new BeanPropertyRowMapper<Event>(Event.class), userId
                                               );
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        try {
            tagList = jdbcTemplate.queryForList("SELECT tag_id FROM event_tag WHERE event_id = ? ", Long.class,
                                                event.getId()
                                               );
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        try {
            eventResponse =
                    EventResponse.builder().id(event.getId()).name(event.getName()).description(event.getDescription())
                                 .seatCount(event.getSeatCount()).startDate(event.getStartDate())
                                 .ticketSellingStartDate(event.getTicketSellingStartDate()).endDate(event.getEndDate())
                                 .createdAt(event.getCreatedAt()).updatedAt(event.getUpdatedAt()).status(
                                         event.getStatus())
                                 .seats(objectMapper.readValue(event.getSeats(),
                                                               new TypeReference<List<HashMap<String, Object>>>() {
                                                               }
                                                              )).tags(new HashSet<>(tagList))
                                 .seatingImageReference(event.getSeatingImageReference()).build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return eventResponse;
    }

}
