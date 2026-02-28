package com.example.demo.fraud;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DynamicFraudToken {
    private final String token;
    private final LocalTime generatedAt;

    public DynamicFraudToken() {
        this.token = UUID.randomUUID().toString();
        this.generatedAt = LocalTime.now();
    }

    public String getToken() {
        return token;
    }

    public LocalTime getGeneratedAt() {
        return generatedAt;
    }

    public String getTokenInfo() {
        return "Token[" + token.substring(0, 8) + "] generated at " + generatedAt;
    }
}
