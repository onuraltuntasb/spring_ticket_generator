package com.springtickergenerator.service;

import com.springtickergenerator.entity.Event;
import com.springtickergenerator.entity.Ticket;
import com.springtickergenerator.exception.ResourceNotFoundException;
import com.springtickergenerator.model.payload.request.TicketRequest;
import com.springtickergenerator.repository.EventRepository;
import com.springtickergenerator.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService{

    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;

    @Override
    public Ticket updateTicket(TicketRequest ticketRequest, Long eventId, Long ticketId) {

        //get ticket from db

        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(
                ()->new ResourceNotFoundException(
                        "There is no ticket with this id :"+ticketId));

        //update fields

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
                .seatNumber(ticketRequest.getSeatNumber())
                .build();

        //add to event

        Event event = eventRepository.findById(eventId)
                .orElseThrow(()-> new ResourceNotFoundException
                        ("There is no event with this id: "+eventId));

        event.getTickets().add(newTicket);

        //save it

        return ticketRepository.save(newTicket);

    }
}
