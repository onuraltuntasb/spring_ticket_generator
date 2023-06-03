package com.springtickergenerator.model.payload.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class EventRequest {
    private String name;
    private String description;
    private Date endDate;
    private Boolean status;
    private int seatCount;
    private String availableSeats;
    private List<Long> tags;
}