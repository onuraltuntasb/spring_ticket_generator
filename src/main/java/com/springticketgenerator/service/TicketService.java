package com.springticketgenerator.service;

import com.springticketgenerator.entity.Ticket;
import com.springticketgenerator.model.payload.request.TicketRequest;

public interface TicketService {

    Ticket updateTicket(TicketRequest ticketRequest, Long eventId, Long ticketId);
    Ticket saveTicket(TicketRequest ticketRequest,Long eventId);
    void deleteTicket(Long ticketId,Long eventId);
}
