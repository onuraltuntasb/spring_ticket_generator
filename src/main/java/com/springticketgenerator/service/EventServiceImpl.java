package com.springticketgenerator.service;

import com.springticketgenerator.entity.Event;
import com.springticketgenerator.entity.Tag;
import com.springticketgenerator.entity.User;
import com.springticketgenerator.exception.ResourceNotFoundException;
import com.springticketgenerator.model.payload.request.EventRequest;
import com.springticketgenerator.repository.EventRepository;
import com.springticketgenerator.repository.TagRepository;
import com.springticketgenerator.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    ZoneOffset zoneOffSet = ZoneOffset.of("+00:00");


    @Transactional
    @Override
    public Event updateEvent(EventRequest eventRequest, Long eventId) {

        Event rEvent = eventRepository.findById(eventId).orElseThrow(
                () -> new ResourceNotFoundException("Event not found with this id : " + eventId)
        );

        OffsetDateTime offsetCurrDateTime = OffsetDateTime.now(zoneOffSet);

        //event start date is after current date
        if (rEvent.getStartDate().isAfter(offsetCurrDateTime)) {

            //event start date is before event end date
            if (rEvent.getStartDate().isBefore(rEvent.getEndDate())) {

                //ticket selling start date is after event start date
                if (rEvent.getTicketSellingStartDate().isBefore(rEvent.getStartDate().minusMinutes(5))) {

                    rEvent.setDescription(eventRequest.getDescription());

                    if(eventRequest.getSeats()!=null){
                        //drop as_eventid and insert data again
                        String tableName = "as_" + eventId;
                        dropASTable(tableName);
                        try {
                            insertAsBatch(tableName, eventRequest.getSeats());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    if (eventRequest.getStatus() != null && rEvent.getStatus() != null) {

                        if (!rEvent.getStatus() && eventRequest.getStatus()) {

                            if (eventRequest.getStartDate() != null && eventRequest.getEndDate() != null
                                    && eventRequest.getTicketSellingStartDate() != null) {

                                if (eventRequest.getStartDate().isAfter(offsetCurrDateTime)) {

                                    if (eventRequest.getStartDate().isBefore(rEvent.getEndDate())) {

                                        if (eventRequest.getTicketSellingStartDate().isBefore(rEvent.getStartDate().minusMinutes(5))) {
                                            rEvent.setStartDate(eventRequest.getStartDate());
                                            rEvent.setEndDate(eventRequest.getEndDate());
                                            rEvent.setTicketSellingStartDate(eventRequest.getTicketSellingStartDate());
                                            rEvent.setStatus(true);
                                        } else {
                                            throw new RuntimeException("Current date must be 5min before ticket selling date");
                                        }

                                    } else {
                                        throw new RuntimeException("Start date is after endDate");
                                    }

                                } else {
                                    throw new RuntimeException("Start date is after endDate");
                                }


                            } else {
                                throw new RuntimeException("Start date or endDate or ticket selling date is null");
                            }
                        } else {
                            rEvent.setStatus(eventRequest.getStatus());
                        }

                    } else {
                        throw new RuntimeException("Event request status or event status is null");
                    }


                    List<Long> requestTags = eventRequest.getTags();
                    Set<Tag> tags = new HashSet<>();

                    if (requestTags != null) {
                        for (Long requestTag : requestTags) {

                            tags.add(tagRepository.findById(requestTag)
                                    .orElseThrow(() -> new ResourceNotFoundException("Tag not found with this id : " + requestTag)));

                        }
                        rEvent.setTags(tags);
                    }
                    if(eventRequest.getDescription()!=null){
                        rEvent.setDescription(eventRequest.getDescription());
                    }


                } else {
                    throw new RuntimeException("Current date must be 5min before ticket selling date");
                }
            } else {
                throw new RuntimeException("Start date is after end date");
            }

        } else {
            throw new RuntimeException("Start date is not after current date!");
        }

        return eventRepository.save(rEvent);

    }


    @Transactional
    @Override
    public Event saveEvent(EventRequest eventRequest, Long userId) {

        List<Long> requestTagIds = eventRequest.getTags();
        Set<Tag> tags = new HashSet<>();

        if (requestTagIds != null) {
            for (Long requestTagId : requestTagIds) {
                tags.add(tagRepository.findById(requestTagId)
                        .orElseThrow(() -> new ResourceNotFoundException("Tag not found with this id :" + requestTagId))
                );

            }
        }


        Event event = null;
        OffsetDateTime offsetCurrDateTime = OffsetDateTime.now(zoneOffSet);


        if (eventRequest.getStartDate() != null && eventRequest.getEndDate() != null
                && eventRequest.getTicketSellingStartDate() != null) {

            //event startDate is after current time
            if (eventRequest.getStartDate().isAfter(offsetCurrDateTime)) {

                //event startDate is before endDate
                if (eventRequest.getStartDate().isBefore(eventRequest.getEndDate())) {

                    //ticketSelling before startDate
                    if (eventRequest.getTicketSellingStartDate().isBefore(eventRequest.getStartDate().minusMinutes(5))) {

                        event = Event.builder()
                                .name(eventRequest.getName())
                                .description(eventRequest.getDescription())
                                .seatCount(eventRequest.getSeats().size())
                                .startDate(eventRequest.getStartDate())
                                .ticketSellingStartDate(eventRequest.getTicketSellingStartDate())
                                .endDate(eventRequest.getEndDate())
                                .createdAt(offsetCurrDateTime)
                                .updatedAt(offsetCurrDateTime)
                                .status(eventRequest.getStatus())
                                .tags(tags)
                                .build();

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


        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User not found with this id : " + userId)
        );

        user.getEvents().add(event);

        Event event1 = eventRepository.saveAndFlush(event);

        String tableName = "as_" + event1.getId();

        //TODO warning - sql injection
        jdbcTemplate.execute("CREATE TABLE " + tableName + " (\n" +
                "    id int NOT NULL AUTO_INCREMENT,\n" +
                "    name varchar(255) NOT NULL,\n" +
                "    PRIMARY KEY (id)\n" +
                ");");


        try {
            insertAsBatch(tableName, eventRequest.getSeats());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        return event1;
    }

    //TODO delete tickets after a certain time, store procedure
    @Override
    public void deleteEvent(Long eventId) {

        Event event = eventRepository.findById(eventId).orElseThrow(() -> new ResourceNotFoundException("event not found with this id!"));

        if (!event.getStatus()) {
            eventRepository.deleteById(eventId);
            dropASTable("as_" + eventId);
        } else {
            throw new RuntimeException("Event status is active, must be disable");
        }
    }

    public void dropASTable(String tableName) {

        try {
            jdbcTemplate.execute("DROP TABLE " + tableName + ";");
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

    }


    public void insertAsBatch(String tableName, List<String> seats) throws SQLException {

        DataSource ds = jdbcTemplate.getDataSource();
        Connection connection = null;

        connection = ds.getConnection();


        connection.setAutoCommit(false);

        String sql = "insert into " + tableName + " (name) values (?)";
        PreparedStatement ps = null;

        ps = connection.prepareStatement(sql);

        final int batchSize = 1000;
        int count = 0;

        for (String seat : seats) {
            ps.setString(1, seat);
            ps.addBatch();

            ++count;

            if (count % batchSize == 0 || count == seats.size()) {
                ps.executeBatch();
                ps.clearBatch();
            }
        }

        connection.commit();
        ps.close();

    }

}
