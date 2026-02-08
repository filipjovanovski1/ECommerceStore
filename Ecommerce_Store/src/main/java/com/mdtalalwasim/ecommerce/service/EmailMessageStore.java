package com.mdtalalwasim.ecommerce.service;

import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

@Component
public class EmailMessageStore {
    private final AtomicReference<EmailMessage> lastMessage = new AtomicReference<>();

    public void store(EmailMessage message) {
        lastMessage.set(message);
    }

    public EmailMessage getLastMessage() {
        return lastMessage.get();
    }

    public void clear() {
        lastMessage.set(null);
    }
}