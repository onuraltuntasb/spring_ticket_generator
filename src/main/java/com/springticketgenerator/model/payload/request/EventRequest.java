package com.springticketgenerator.model.payload.request;

import jakarta.persistence.Convert;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class EventRequest {
    private String name;
    private String description;
    private List<HashMap<String, Object>> seats;
    private OffsetDateTime startDate;
    private OffsetDateTime ticketSellingStartDate;
    private OffsetDateTime endDate;
    private Boolean status;
    private Set<Long> tags;
    private String seatingImageReference;


}