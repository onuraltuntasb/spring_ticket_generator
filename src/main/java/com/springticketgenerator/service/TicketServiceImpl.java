package com.springticketgenerator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springticketgenerator.entity.Event;
import com.springticketgenerator.entity.Ticket;
import com.springticketgenerator.entity.User;
import com.springticketgenerator.exception.ResourceNotFoundException;
import com.springticketgenerator.model.payload.request.TicketRequest;
import com.springticketgenerator.model.payload.response.GetAllTicketResponse;
import com.springticketgenerator.repository.EventRepository;
import com.springticketgenerator.repository.TicketRepository;
import com.springticketgenerator.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;
    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    private final EventService eventService;
    private final ObjectMapper objectMapper;

    ZoneOffset zoneOffSet = ZoneOffset.of("+00:00");

    @Override
    public Ticket updateTicket(TicketRequest ticketRequest, Long eventId, Long ticketId) {

        OffsetDateTime offsetCurrDateTime = OffsetDateTime.now(zoneOffSet);

        //get ticket from db

        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(
                () -> new ResourceNotFoundException(
                        "There is no ticket with this id :" + ticketId));


        if (!ticketRequest.getSeatNumber().equals(ticket.getSeatNumber())) {

            String seats = null;
            List<HashMap<String, Object>> seatsList = new ArrayList<>();
            List<HashMap<String, Object>> seatsListParsed = new ArrayList<>();

            try {
                seats = jdbcTemplate.queryForObject("SELECT seats FROM event WHERE id=?", String.class, eventId);
                try {
                    seatsList = objectMapper.readValue(seats,
                                                       new TypeReference<List<HashMap<String, Object>>>() {
                                                       }
                                                      );
                } catch (JsonProcessingException e) {
                    log.info("65", e);
                    throw new RuntimeException(e);
                }
            } catch (DataAccessException e) {
                log.info("148", e);
                throw new RuntimeException(e);
            }


            for (HashMap<String, Object> el : seatsList) {

                for (Map.Entry<String, Object> entry : el.entrySet()) {
                    if (entry.getKey().equals("name")) {
                        if (entry.getValue().equals(ticketRequest.getSeatNumber())) {

                            if (Boolean.valueOf(el.get("isReserved").toString())) {
                                throw new RuntimeException("Ticket is reserved!");

                            } else {


                                HashMap<String, Object> hMap = new HashMap<String, Object>();
                                hMap.put("name", ticketRequest.getSeatNumber());
                                hMap.put("isReserved", true);
                                seatsListParsed.add(hMap);


                            }

                        } else if (entry.getValue().equals(ticket.getSeatNumber())) {
                            HashMap<String, Object> hMap = new HashMap<String, Object>();
                            hMap.put("name", ticket.getSeatNumber());
                            hMap.put("isReserved", false);
                            seatsListParsed.add(hMap);
                        } else {
                            seatsListParsed.add(el);
                        }

                    }

                }

            }

            try {
                seats = objectMapper.writeValueAsString(seatsListParsed);
            } catch (JsonProcessingException e) {
                log.info("99", e);
                throw new RuntimeException(e);
            }

            try {
                jdbcTemplate.update("UPDATE event SET seats = ? WHERE id = ?", seats, eventId);
            } catch (DataAccessException e) {
                log.info("106", e);
                throw new RuntimeException(e);
            }

        }


        ticket.setName(ticketRequest.getName());
        ticket.setSeatNumber(ticketRequest.getSeatNumber());
        ticket.setUpdatedAt(offsetCurrDateTime);

        //save it

        return ticketRepository.save(ticket);

    }

    @Override
    public Ticket saveTicket(TicketRequest ticketRequest, Long eventId, User user) {

        //create uuid

        UUID uuid = UUID.randomUUID();
        String uuidAsString = uuid.toString();

        //ticketRequest to new ticket mapping

        OffsetDateTime offsetCurrDateTime = OffsetDateTime.now(zoneOffSet);

        Ticket newTicket = Ticket.builder()
                                 .barcode(uuidAsString)
                                 .name(ticketRequest.getName())
                                 .seatNumber(ticketRequest.getSeatNumber())
                                 .createdAt(offsetCurrDateTime)
                                 .updatedAt(offsetCurrDateTime)
                                 .build();

        //add to event

        Event event = eventRepository.findById(eventId)
                                     .orElseThrow(() -> new ResourceNotFoundException
                                             ("There is no event with this id: " + eventId));


        event.getTickets().add(newTicket);
        user.getTickets().add(newTicket);

        //save it

        Ticket rTicket = ticketRepository.save(newTicket);

        //remove from available seats

        //get seat string


        String seats = null;
        List<HashMap<String, Object>> seatsList = new ArrayList<>();
        List<HashMap<String, Object>> seatsListParsed = new ArrayList<>();

        try {
            seats = jdbcTemplate.queryForObject("SELECT seats FROM event WHERE id=?", String.class, eventId);
            try {
                seatsList = objectMapper.readValue(seats,
                                                   new TypeReference<List<HashMap<String, Object>>>() {
                                                   }
                                                  );
            } catch (JsonProcessingException e) {
                log.info("145", e);
                throw new RuntimeException(e);
            }
        } catch (DataAccessException e) {
            log.info("148", e);
            throw new RuntimeException(e);
        }


        for (HashMap<String, Object> el : seatsList) {

            for (Map.Entry<String, Object> entry : el.entrySet()) {
                if (entry.getKey().equals("name")) {
                    if (entry.getValue().equals(ticketRequest.getSeatNumber())) {

                        HashMap<String, Object> hMap = new HashMap<String, Object>();
                        hMap.put("name", ticketRequest.getSeatNumber());
                        hMap.put("isReserved", true);
                        seatsListParsed.add(hMap);


                    } else {
                        seatsListParsed.add(el);
                    }

                }

            }

        }

        try {
            seats = objectMapper.writeValueAsString(seatsListParsed);
        } catch (JsonProcessingException e) {
            log.info("177", e);
            throw new RuntimeException(e);
        }

        try {
            jdbcTemplate.update("UPDATE event SET seats = ? WHERE id = ?", seats, eventId);
        } catch (DataAccessException e) {
            log.info("184", e);
            throw new RuntimeException(e);
        }

        return rTicket;

    }

    @Override
    public void deleteTicket(Long ticketId, Long eventId) {


        //get ticket from db

        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(
                () -> new ResourceNotFoundException(
                        "There is no ticket with this id :" + ticketId));


        String seats = null;
        List<HashMap<String, Object>> seatsList = new ArrayList<>();
        List<HashMap<String, Object>> seatsListParsed = new ArrayList<>();

        try {
            seats = jdbcTemplate.queryForObject("SELECT seats FROM event WHERE id=?", String.class, eventId);
            try {
                seatsList = objectMapper.readValue(seats,
                                                   new TypeReference<List<HashMap<String, Object>>>() {
                                                   }
                                                  );
            } catch (JsonProcessingException e) {
                log.info("65", e);
                throw new RuntimeException(e);
            }
        } catch (DataAccessException e) {
            log.info("148", e);
            throw new RuntimeException(e);
        }


        for (HashMap<String, Object> el : seatsList) {

            for (Map.Entry<String, Object> entry : el.entrySet()) {
                if (entry.getKey().equals("name")) {
                    if (entry.getValue().equals(ticket.getSeatNumber())) {
                        HashMap<String, Object> hMap = new HashMap<String, Object>();
                        hMap.put("name", ticket.getSeatNumber());
                        hMap.put("isReserved", false);
                        seatsListParsed.add(hMap);
                    } else {
                        seatsListParsed.add(el);
                    }

                }

            }

        }

        try {
            seats = objectMapper.writeValueAsString(seatsListParsed);
        } catch (JsonProcessingException e) {
            log.info("99", e);
            throw new RuntimeException(e);
        }

        try {
            jdbcTemplate.update("UPDATE event SET seats = ? WHERE id = ?", seats, eventId);
        } catch (DataAccessException e) {
            log.info("106", e);
            throw new RuntimeException(e);
        }


        ticketRepository.deleteById(ticketId);


    }

    @Override
    public List<GetAllTicketResponse> findAllByUserId(Long userId) {


        String getSql = "SELECT * FROM ticket WHERE user_id =? ";
        List<GetAllTicketResponse> ticketList = null;

        try {
            ticketList = jdbcTemplate.query(getSql,
                                            (rs, rowNum) ->
                                                    new GetAllTicketResponse(
                                                            rs.getLong("id"),
                                                            rs.getString("name"),
                                                            rs.getString("barcode"),
                                                            rs.getString("seat_number"),
                                                            rs.getLong("user_id"),
                                                            rs.getTimestamp("updated_at"),
                                                            rs.getTimestamp("created_at"),
                                                            eventService.getEventByUserId(rs.getLong("event_id"))

                                                    ), userId
                                           );

        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        System.out.println("ticketList: " + ticketList);

        return ticketList;
    }
}
