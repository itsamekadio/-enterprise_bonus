package com.example.demo.service;

import com.example.demo.audit.BrokenRequestBean;
import com.example.demo.fraud.TrappedPrototypeBean;
import com.example.demo.session.BrokenSessionBean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class BrokenScopeService {

    private final BrokenRequestBean brokenRequest;
    private final BrokenSessionBean brokenSession;
    private final TrappedPrototypeBean trappedPrototype;

    // We use @Lazy for Request/Session so the app starts, but crashes at RUNTIME
    // when called
    public BrokenScopeService(@Lazy BrokenRequestBean brokenRequest,
            @Lazy BrokenSessionBean brokenSession,
            TrappedPrototypeBean trappedPrototype) {
        this.brokenRequest = brokenRequest;
        this.brokenSession = brokenSession;
        this.trappedPrototype = trappedPrototype;
    }

    public String getBrokenRequestInfo() {
        // Without proxy, Spring tries to find the 'real' bean now.
        // Even in a web thread, it fails because the singleton field doesn't have a
        // dynamic lookup mechanism.
        return "Broken Request ID: " + brokenRequest.getId();
    }

    public String getBrokenSessionInfo() {
        return "Broken Session ID: " + brokenSession.getId();
    }

    public String getTrappedPrototypeInfo() {
        // This will always return the SAME ID because it was injected ONCE at startup
        return "Trapped Prototype ID: " + trappedPrototype.getId();
    }
}
