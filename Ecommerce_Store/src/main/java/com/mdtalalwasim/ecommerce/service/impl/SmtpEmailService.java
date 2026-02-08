package com.mdtalalwasim.ecommerce.service.impl;

import java.io.UnsupportedEncodingException;

import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.mdtalalwasim.ecommerce.config.MailProperties;
import com.mdtalalwasim.ecommerce.service.EmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@Profile("!test & !e2e")
public class SmtpEmailService implements EmailService {
    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    public SmtpEmailService(JavaMailSender mailSender, MailProperties mailProperties) {
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
    }

    @Override
    public boolean sendPasswordResetEmail(String recipientEmail, String resetUrl)
            throws UnsupportedEncodingException, MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message, "UTF-8");

        messageHelper.setFrom(mailProperties.getFromAddress(), mailProperties.getFromName());
        messageHelper.setTo(recipientEmail);
        messageHelper.setSubject(mailProperties.getResetSubject());
        messageHelper.setText(buildResetEmailBody(resetUrl), true);

        mailSender.send(message);
        return true;
    }

    private String buildResetEmailBody(String resetUrl) {
        return "<p>Hello, </p>" + "<p>You have requested to reset your password.</p>"
                + "<p>Please click the link to change your password:</p>"
                + "<p><a href=\"" + resetUrl + "\">Change my password</a></p>";
    }
}