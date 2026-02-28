package com.example.demo.fraud;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE) // No Proxy
public class TrappedPrototypeBean {
    private final String id;

    public TrappedPrototypeBean() {
        this.id = UUID.randomUUID().toString().substring(0, 8);
    }

    public String getId() {
        return id;
    }
}
