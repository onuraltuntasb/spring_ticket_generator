package com.springtickergenerator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.springtickergenerator.entity.Event;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event,Long> {
    Set<Event> findEventsByTagsIn(Set<Long> tags);

}
