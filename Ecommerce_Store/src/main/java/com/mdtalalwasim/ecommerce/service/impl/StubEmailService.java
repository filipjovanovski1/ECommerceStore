package com.mdtalalwasim.ecommerce.service.impl;

import java.io.UnsupportedEncodingException;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.mdtalalwasim.ecommerce.config.MailProperties;
import com.mdtalalwasim.ecommerce.service.EmailMessage;
import com.mdtalalwasim.ecommerce.service.EmailMessageStore;
import com.mdtalalwasim.ecommerce.service.EmailService;

import jakarta.mail.MessagingException;

@Service
@Profile({"test", "e2e"})
public class StubEmailService implements EmailService {
    private final EmailMessageStore messageStore;
    private final MailProperties mailProperties;

    public StubEmailService(EmailMessageStore messageStore, MailProperties mailProperties) {
        this.messageStore = messageStore;
        this.mailProperties = mailProperties;
    }

    @Override
    public boolean sendPasswordResetEmail(String recipientEmail, String resetUrl)
            throws UnsupportedEncodingException, MessagingException {
        String subject = mailProperties.getResetSubject();
        String body = "Password reset link: " + resetUrl;
        messageStore.store(new EmailMessage(recipientEmail, subject, body, resetUrl));
        return true;
    }
}