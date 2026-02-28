package com.example.demo.service;

import com.example.demo.audit.SecureAuditLog;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncAuditService {
    private final SecureAuditLog auditLog;

    public AsyncAuditService(SecureAuditLog auditLog) {
        this.auditLog = auditLog;
    }

    @Async("asyncExecutor")
    public java.util.concurrent.CompletableFuture<String> logBrokenAsyncTransaction() {
        // This will CRASH with ScopeNotActiveException/IllegalStateException
        return java.util.concurrent.CompletableFuture.completedFuture(
                "Async Audit (Should Fail): " + auditLog.getInfo());
    }

    @Async("asyncExecutor")
    public java.util.concurrent.CompletableFuture<String> logManualAsyncTransaction(
            org.springframework.web.context.request.RequestAttributes attributes) {
        // Manually setting the context inside the worker thread
        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(attributes);
        try {
            return java.util.concurrent.CompletableFuture.completedFuture(
                    "Manual Async Success: " + auditLog.getInfo());
        } finally {
            org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
        }
    }
}
