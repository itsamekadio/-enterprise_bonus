package com.example.demo.audit;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.UUID;

@Component
@RequestScope // Default: No Proxy (proxyMode = ScopedProxyMode.NO)
public class BrokenRequestBean {
    private final String id;

    public BrokenRequestBean() {
        this.id = UUID.randomUUID().toString().substring(0, 8);
    }

    public String getId() {
        return id;
    }
}
