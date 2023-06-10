package com.springticketgenerator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.springticketgenerator.entity.Event;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event,Long> {
    Set<Event> findEventsByTagsIn(Set<Long> tags);

}
