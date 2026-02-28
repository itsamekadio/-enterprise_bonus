package com.example.demo.session;

import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.UUID;

@Component
@SessionScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ScopedSessionData {
    private final String sessionId;
    private String username = "Anonymous_Banker";

    public ScopedSessionData() {
        this.sessionId = UUID.randomUUID().toString().substring(0, 8);
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getUsername() {
        return username;
    }

    public void login(String username) {
        this.username = username;
    }
}
