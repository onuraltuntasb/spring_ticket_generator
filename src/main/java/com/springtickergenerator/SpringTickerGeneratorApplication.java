package com.springtickergenerator;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

import static java.util.logging.Level.INFO;

@EntityScan(basePackages = {"com.springtickergenerator.*"})
@SpringBootApplication
public class SpringTickerGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringTickerGeneratorApplication.class, args);
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }



}
