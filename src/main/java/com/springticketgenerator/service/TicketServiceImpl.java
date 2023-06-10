package com.springticketgenerator.service;

import com.springticketgenerator.entity.AvailableTicket;
import com.springticketgenerator.entity.Event;
import com.springticketgenerator.entity.Ticket;
import com.springticketgenerator.exception.ResourceNotFoundException;
import com.springticketgenerator.model.payload.request.TicketRequest;
import com.springticketgenerator.repository.EventRepository;
import com.springticketgenerator.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService{

    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Ticket updateTicket(TicketRequest ticketRequest, Long eventId, Long ticketId) {

        //get ticket from db

        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(
                ()->new ResourceNotFoundException(
                        "There is no ticket with this id :"+ticketId));

        //update fields

        String tableName = "as_"+eventId;
        String getSql = "SELECT * FROM "+tableName+" WHERE name = ?";
        Optional<AvailableTicket> availableTicket = Optional.empty();

        try{
            availableTicket =  jdbcTemplate.query(getSql,new AvailableTicketRowMapper(),ticketRequest.getSeatNumber())
                    .stream()
                    .findFirst();
        }catch (DataAccessException e){
            throw new RuntimeException(e);
        }

        availableTicket.orElseThrow(()->new ResourceNotFoundException("availableTicker is not found with this id :"+ticketRequest.getSeatNumber()));

        String deleteSql = "DELETE FROM "+tableName+" WHERE name = ?";

        try{
            jdbcTemplate.update(deleteSql,ticketRequest.getSeatNumber()) ;
        }catch (DataAccessException e){
            throw new RuntimeException(e);
        }

        ticket.setSeatNumber(ticketRequest.getSeatNumber());
        //save it

        return ticketRepository.save(ticket);

    }

    @Override
    public Ticket saveTicket(TicketRequest ticketRequest, Long eventId) {

        //create uuid

        UUID uuid = UUID.randomUUID();
        String uuidAsString = uuid.toString();

        //ticketRequest to new ticket mapping

        Ticket newTicket = Ticket.builder()
                .barcode(uuidAsString)
                .name(ticketRequest.getName())
                .description(ticketRequest.getDescription())
                .seatNumber(ticketRequest.getSeatNumber())
                .build();

        //add to event

        Event event = eventRepository.findById(eventId)
                .orElseThrow(()-> new ResourceNotFoundException
                        ("There is no event with this id: "+eventId));

        event.getTickets().add(newTicket);

        //save it

        Ticket rTicket = ticketRepository.save(newTicket);

        //remove from available seats

        String tableName = "as_"+eventId;
        String deleteSql = "DELETE FROM "+tableName+" WHERE name = ?";

        try{
            jdbcTemplate.update(deleteSql,ticketRequest.getSeatNumber()) ;
        }catch (DataAccessException e){
            throw new RuntimeException(e);
        }

        return rTicket;

    }

    @Override
    public void deleteTicket(Long ticketId,Long eventId) {

        //ticket can delete 1h before event startDate
        String tableName = "as_"+eventId;
        String deleteSql = "DELETE FROM "+tableName+" WHERE name = ?";

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(()->new ResourceNotFoundException("ticket not found with this id:"+ticketId));

        try{
            jdbcTemplate.update(deleteSql,ticket.getSeatNumber()) ;
        }catch (DataAccessException e){
            throw new RuntimeException(e);
        }

        ticketRepository.deleteById(ticketId);

        //refund money

    }
}
