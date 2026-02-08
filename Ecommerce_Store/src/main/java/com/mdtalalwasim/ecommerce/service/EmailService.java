package com.mdtalalwasim.ecommerce.service;

import java.io.UnsupportedEncodingException;

import jakarta.mail.MessagingException;

public interface EmailService {
    boolean sendPasswordResetEmail(String recipientEmail, String resetUrl)
            throws UnsupportedEncodingException, MessagingException;
}