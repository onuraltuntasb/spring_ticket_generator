package com.springtickergenerator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.springtickergenerator.entity.Event;

public interface EventRepository extends JpaRepository<Event,Long> {
}
