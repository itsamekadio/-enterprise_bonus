package com.example.demo.audit;

import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SecureAuditLog {
    private final String id;
    private final String threadName;

    public SecureAuditLog() {
        this.id = java.util.UUID.randomUUID().toString().substring(0, 8);
        this.threadName = Thread.currentThread().getName();
    }

    public String getId() {
        return id;
    }

    public String getThreadName() {
        return threadName;
    }

    public String getInfo() {
        return "AuditLog[" + id + "] on Thread[" + threadName + "]";
    }
}
