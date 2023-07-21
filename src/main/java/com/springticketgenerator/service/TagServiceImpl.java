package com.springticketgenerator.service;


import com.springticketgenerator.entity.Event;
import com.springticketgenerator.entity.Tag;
import com.springticketgenerator.exception.ResourceNotFoundException;
import com.springticketgenerator.repository.EventRepository;
import com.springticketgenerator.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final EventRepository eventRepository;

    @Override
    public Tag updateTag(Tag tag, Long tagId) {

        Tag rTag = tagRepository.findById(tagId).orElseThrow(
                () -> new ResourceNotFoundException("Tag not found with this id :" + tagId));

        rTag.setName(tag.getName());

        return tagRepository.save(rTag);

    }

    @Override
    public void deleteTag(Long tagId) {
        Set<Long> tags = new HashSet<>();

        tags.add(tagId);

        Set<Event> events = eventRepository.findEventsByTagsIn(tags);

        for (Event event : events) {
            event.removeTag(tagId);
        }

        tagRepository.deleteById(tagId);

    }
}
