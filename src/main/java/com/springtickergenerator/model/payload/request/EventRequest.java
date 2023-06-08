package com.springtickergenerator.model.payload.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class EventRequest {
    private String name;
    private String description;
    private List<String> seats;
    private OffsetDateTime startDate;
    private OffsetDateTime ticketSellingStartDate;
    private OffsetDateTime endDate;
    private Boolean status;
    private List<Long> tags;
}