package com.springtickergenerator.service;

import com.springtickergenerator.entity.Ticket;
import com.springtickergenerator.model.payload.request.TicketRequest;

public interface TicketService {

    Ticket updateTicket(TicketRequest ticketRequest, Long eventId, Long ticketId);

    Ticket saveTicket(TicketRequest ticketRequest,Long eventId);
}
