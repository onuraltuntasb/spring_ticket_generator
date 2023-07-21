package com.springticketgenerator;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;

import java.util.TimeZone;

@EntityScan(basePackages = {"com.springticketgenerator.*"})
@SpringBootApplication
public class SpringTicketGeneratorApplication {

    public static void main(String[] args) {

        SpringApplication.run(SpringTicketGeneratorApplication.class, args);
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+0:00"));
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

}
