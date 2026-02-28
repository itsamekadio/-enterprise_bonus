package com.example.demo.session;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.UUID;

@Component
@SessionScope // Default: No Proxy
public class BrokenSessionBean {
    private final String id;

    public BrokenSessionBean() {
        this.id = UUID.randomUUID().toString().substring(0, 8);
    }

    public String getId() {
        return id;
    }
}
