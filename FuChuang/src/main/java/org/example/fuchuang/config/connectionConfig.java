package org.example.fuchuang.config;

import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

public class connectionConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
