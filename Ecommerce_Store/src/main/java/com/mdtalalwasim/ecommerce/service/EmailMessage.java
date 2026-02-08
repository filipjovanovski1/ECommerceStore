package com.mdtalalwasim.ecommerce.service;

public class EmailMessage {
    private final String recipient;
    private final String subject;
    private final String body;
    private final String resetUrl;

    public EmailMessage(String recipient, String subject, String body, String resetUrl) {
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
        this.resetUrl = resetUrl;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public String getResetUrl() {
        return resetUrl;
    }
}