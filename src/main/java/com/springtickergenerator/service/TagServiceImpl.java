package com.springtickergenerator.service;


import com.springtickergenerator.entity.Event;
import com.springtickergenerator.entity.Tag;
import com.springtickergenerator.exception.ResourceNotFoundException;
import com.springtickergenerator.repository.EventRepository;
import com.springtickergenerator.repository.TagRepository;
import jakarta.websocket.server.ServerEndpoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class TagServiceImpl implements TagService{

    private final TagRepository tagRepository;
    private final EventRepository eventRepository;

    @Override
    public Tag updateTag(Tag tag, Long tagId) {

        Tag rTag = tagRepository.findById(tagId).orElseThrow(()-> new ResourceNotFoundException("Tag not found with this id :"+tagId));

        rTag.setName(tag.getName());

        return tagRepository.save(rTag);

    }

    @Override
    public void deleteTag(Long tagId) {
        Set<Long> tags = new HashSet<>();

        tags.add(tagId);

        Set<Event> events = eventRepository.findEventsByTagsIn(tags);

        for (Event event:events) {
            event.removeTag(tagId);
        }

        tagRepository.deleteById(tagId);


    }
}
