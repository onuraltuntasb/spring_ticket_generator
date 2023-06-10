package com.springticketgenerator.service;


import com.springticketgenerator.entity.AvailableTicket;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AvailableTicketRowMapper implements RowMapper<AvailableTicket> {
    @Override
    public AvailableTicket mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new AvailableTicket(
                rs.getLong("id"),
                rs.getString("name")
        );
    }
}