package com.springticketgenerator.model.payload.response;

import com.springticketgenerator.entity.Event;
import lombok.*;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class GetAllEventsResponse {
    private List<Event> eventList;
}
