package com.springticketgenerator.setup;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
public class TicketProperties {

    private String jwtSecret ="secretKey";
    private String jwtExpirationMs ="86400000";
    private String jwtRefreshExpirationSecond ="25920000";


    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public String getJwtExpirationMs() {
        return jwtExpirationMs;
    }

    public void setJwtExpirationMs(String jwtExpirationMs) {
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public String getJwtRefreshExpirationSecond() {
        return jwtRefreshExpirationSecond;
    }

    public void setJwtRefreshExpirationSecond(String jwtRefreshExpirationSecond) {
        this.jwtRefreshExpirationSecond = jwtRefreshExpirationSecond;
    }
}