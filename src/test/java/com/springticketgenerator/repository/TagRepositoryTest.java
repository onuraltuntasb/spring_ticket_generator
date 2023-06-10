package com.springticketgenerator.repository;

import com.springticketgenerator.entity.Event;
import com.springticketgenerator.entity.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TagRepositoryTest {

    @Container
    MySQLContainer mySQLContainer = (MySQLContainer) new MySQLContainer("mysql:latest")
            .withDatabaseName("spring_ticket_generator")
            .withUsername("root")
            .withPassword("root");

    @Autowired
    private TagRepository tagRepository;

    @Test
    public void shouldSaveTag(){

        Set<Event> eventSet = new HashSet<>();

        Tag expectedTag = Tag.builder()
                .id(null)
                .name("tag1")
                .events(eventSet)
                .build();

        Tag actualTag = tagRepository.save(expectedTag);
        assertThat(actualTag).usingRecursiveComparison()
                .ignoringFields("id").isEqualTo(expectedTag);

    }

}
