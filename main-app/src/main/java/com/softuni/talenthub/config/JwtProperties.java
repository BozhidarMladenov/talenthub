package com.softuni.talenthub.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret = "TalentHub-Default-Secret-Key-2026-Must-Be-256-Bits-Long!!";
    private long expirationMs = 86400000;
}
