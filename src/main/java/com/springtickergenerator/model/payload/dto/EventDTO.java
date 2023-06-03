package com.springtickergenerator.model.payload.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class EventDTO {
    private String name;
    private String description;
    private Date endDate;
    private int seatCount;
    private String availableSeats;
    private List<Long> tags;
}
