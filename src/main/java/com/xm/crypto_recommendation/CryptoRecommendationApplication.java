package com.xm.crypto_recommendation;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
        info = @Info(
                title = "Crypto Recommendation Service",
                description = "API for crypto price statistics and recommendations",
                version = "1.0"
        )
)
@SpringBootApplication
public class CryptoRecommendationApplication {

    public static void main(String[] args) {
        SpringApplication.run(CryptoRecommendationApplication.class, args);
    }
}
