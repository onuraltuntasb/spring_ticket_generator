package com.springtickergenerator;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;

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
