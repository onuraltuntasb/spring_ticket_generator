package com.springticketgenerator.service;

import com.springticketgenerator.entity.Ticket;
import com.springticketgenerator.entity.User;
import com.springticketgenerator.model.payload.request.TicketRequest;
import com.springticketgenerator.model.payload.response.GetAllTicketResponse;

import java.util.List;

public interface TicketService {

    Ticket updateTicket(TicketRequest ticketRequest, Long eventId, Long ticketId);

    Ticket saveTicket(TicketRequest ticketRequest, Long eventId, User user);

    void deleteTicket(Long ticketId, Long eventId);

    List<GetAllTicketResponse> findAllByUserId(Long userId);

}
