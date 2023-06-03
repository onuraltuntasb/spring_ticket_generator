package com.springtickergenerator.service;

import com.springtickergenerator.entity.Event;
import com.springtickergenerator.entity.Tag;
import com.springtickergenerator.entity.User;
import com.springtickergenerator.exception.ResourceNotFoundException;
import com.springtickergenerator.model.payload.request.EventRequest;
import com.springtickergenerator.repository.EventRepository;
import com.springtickergenerator.repository.TagRepository;
import com.springtickergenerator.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService{

    private final EventRepository eventRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    @Override
    public Event updateEvent(EventRequest eventRequest, Long eventId) {
        Event rEvent = eventRepository.findById(eventId).orElseThrow(
                ()->new ResourceNotFoundException("Event not found with this id : "+ eventId)
        );

        rEvent.setDescription(eventRequest.getDescription());
        //TODO needs return ticket option
        rEvent.setStatus(eventRequest.getStatus());

        List<Long> requestTags = eventRequest.getTags();
        Set<Tag> tags = new HashSet<>();

        if(requestTags!=null){
            for (Long requestTag : requestTags) {
                log.info("tagId : {}", requestTag);

                tags.add(tagRepository.findById(requestTag)
                        .orElseThrow(() -> new ResourceNotFoundException("Tag not found with this id : " + requestTag)));

            }
        }
        rEvent.setTags(tags);


        return eventRepository.save(rEvent);
    }

    @Override
    public Event saveEvent(EventRequest eventRequest, Long userId) {

        List<Long> requestTagIds = eventRequest.getTags();
        Set<Tag> tags = new HashSet<>();

        if(requestTagIds != null){
            for (Long requestTagId : requestTagIds) {
                log.info("tagId : {}", requestTagId);
                tags.add(tagRepository.findById(requestTagId)
                        .orElseThrow(() -> new ResourceNotFoundException("Tag not found with this id :" + requestTagId))
                );

            }
        }

        Date date = new Date();

        User user = userRepository.findById(userId).orElseThrow(
                ()->new ResourceNotFoundException("User not found with this id : "+ userId)
        );

        Event event = Event.builder()
                .availableSeats(eventRequest.getAvailableSeats())
                .description(eventRequest.getDescription())
                .status(eventRequest.getStatus())
                .seatCount(eventRequest.getSeatCount())
                .endDate(eventRequest.getEndDate())
                .createdAt(date)
                .updatedAt(date)
                .tags(tags)
                .build();

        user.getEvents().add(event);
        return eventRepository.save(event);
    }
}
