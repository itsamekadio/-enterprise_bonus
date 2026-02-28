package com.example.demo.service;

import com.example.demo.audit.SecureAuditLog;
import com.example.demo.fraud.DynamicFraudToken;
import com.example.demo.session.ScopedSessionData;
import org.springframework.stereotype.Service;

@Service
public class BankTransferService {
    private final SecureAuditLog auditLog;
    private final ScopedSessionData sessionData;
    private final DynamicFraudToken fraudToken;

    public BankTransferService(SecureAuditLog auditLog, ScopedSessionData sessionData, DynamicFraudToken fraudToken) {
        this.auditLog = auditLog;
        this.sessionData = sessionData;
        this.fraudToken = fraudToken;
    }

    public String performTransfer(double amount) {
        String logInfo = auditLog.getInfo();
        String user = sessionData.getUsername();
        String token = fraudToken.getTokenInfo();

        return String.format(
                "Transfer of $%.2f initiated by %s. [%s]. Using %s",
                amount, user, logInfo, token);
    }

    public String getCurrentSessionInfo() {
        return "SessionID: " + sessionData.getSessionId() + " for user: " + sessionData.getUsername();
    }
}
