package com.springticketgenerator.model.payload.response;

import com.springticketgenerator.entity.Tag;
import com.springticketgenerator.entity.Ticket;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Builder
public class EventResponse {

    private Long id;

    private String name;

    private String description;

    private int seatCount;

    private OffsetDateTime startDate;

    private OffsetDateTime ticketSellingStartDate;

    private OffsetDateTime endDate;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    private Boolean status;

    private Set<Long> tags = new HashSet<>();

    private Set<Ticket> tickets = new HashSet<>();

    private List<HashMap<String, Object>> seats;
    private String seatingImageReference;


}

