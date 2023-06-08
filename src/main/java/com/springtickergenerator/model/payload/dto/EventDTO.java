package com.springtickergenerator.model.payload.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class EventDTO {
    private String name;
    private String description;
    private int seatCount;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private List<Long> tags;
}
