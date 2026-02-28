package com.example.demo.controller;

import com.example.demo.service.AsyncAuditService;
import com.example.demo.service.BankTransferService;
import com.example.demo.session.ScopedSessionData;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/bank")
public class BankController {
    private final BankTransferService transferService;
    private final AsyncAuditService asyncAuditService;
    private final ScopedSessionData sessionData;
    private final com.example.demo.service.BrokenScopeService brokenService;

    public BankController(BankTransferService transferService, AsyncAuditService asyncAuditService,
            ScopedSessionData sessionData, com.example.demo.service.BrokenScopeService brokenService) {
        this.transferService = transferService;
        this.asyncAuditService = asyncAuditService;
        this.sessionData = sessionData;
        this.brokenService = brokenService;
    }

    @PostMapping("/transfer")
    public Map<String, String> transfer(@RequestParam(defaultValue = "100.0") double amount) {
        String result = transferService.performTransfer(amount);
        return Map.of("status", "Success", "message", result, "thread", Thread.currentThread().getName());
    }

    @GetMapping("/session")
    public Map<String, String> getSession() {
        return Map.of("info", transferService.getCurrentSessionInfo(), "thread", Thread.currentThread().getName());
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestParam String username) {
        sessionData.login(username);
        return Map.of("status", "Logged In", "user", username);
    }

    @GetMapping("/broken-request")
    public Map<String, String> brokenRequest() {
        return Map.of("result", brokenService.getBrokenRequestInfo());
    }

    @GetMapping("/broken-session")
    public Map<String, String> brokenSession() {
        return Map.of("result", brokenService.getBrokenSessionInfo());
    }

    @GetMapping("/broken-prototype")
    public Map<String, String> brokenPrototype() {
        return Map.of("result", brokenService.getTrappedPrototypeInfo());
    }

    @GetMapping("/broken-async")
    public CompletableFuture<Map<String, String>> brokenAsync() {
        return asyncAuditService.logBrokenAsyncTransaction()
                .thenApply(res -> Map.of("status", "Async Success", "details", res))
                .exceptionally(ex -> Map.of("status", "Async Error", "error", ex.getCause().getMessage()));
    }

    @GetMapping("/manual-async")
    public CompletableFuture<Map<String, String>> manualAsync() {
        // Capture context in the main thread
        org.springframework.web.context.request.RequestAttributes attributes = org.springframework.web.context.request.RequestContextHolder
                .getRequestAttributes();

        return asyncAuditService.logManualAsyncTransaction(attributes)
                .thenApply(res -> Map.of("status", "Async Success", "details", res))
                .exceptionally(ex -> Map.of("status", "Async Error", "error", ex.getMessage()));
    }
}
