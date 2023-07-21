package com.springticketgenerator.model.payload.response;

import lombok.*;
import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class GetAllTicketResponse {


    private Long id;

    private String name;

    private String barcode;

    private String seatNumber;

    private Long userId;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    private EventResponse event;

}
