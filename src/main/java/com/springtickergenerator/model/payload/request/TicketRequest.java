package com.springtickergenerator.model.payload.request;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class TicketRequest {
    private String name;
    private String seatNumber;
    private String description;

}

