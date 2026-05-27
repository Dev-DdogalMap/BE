package com.ddogalmap.global.config.swagger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerLogConfig implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(SwaggerLogConfig.class);

    @Override
    public void run(ApplicationArguments args) {
        logger.info("==========================================================");
        logger.info("Swagger UI: http://localhost:8080/swagger-ui/index.html");
        logger.info("==========================================================");
    }
}