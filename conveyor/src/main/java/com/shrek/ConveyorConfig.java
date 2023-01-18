package com.shrek;


import com.shrek.model.LoanApplicationRequestDTO;
import com.shrek.model.ScoringDataDTO;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;


@Configuration


public class ConveyorConfig{

    @Bean
    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder () {return new Jackson2ObjectMapperBuilder();
    }

}







