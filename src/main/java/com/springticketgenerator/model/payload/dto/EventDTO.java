package com.springticketgenerator.model.payload.dto;

import com.springticketgenerator.entity.Tag;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class EventDTO {
    private Long id;
    private String name;
    private String description;
    private int seatCount;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private OffsetDateTime ticketSellingStartDate;
    private List<Tag> tags;
}
