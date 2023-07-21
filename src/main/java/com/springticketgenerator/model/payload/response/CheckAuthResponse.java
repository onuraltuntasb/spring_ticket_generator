package com.springticketgenerator.model.payload.response;

import com.springticketgenerator.entity.Event;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckAuthResponse {
    private Set<Event> eventSet;
}