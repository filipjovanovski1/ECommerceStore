package com.mdtalalwasim.ecommerce.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mdtalalwasim.ecommerce.service.EmailMessage;
import com.mdtalalwasim.ecommerce.service.EmailMessageStore;

@RestController
@Profile({"test", "e2e"})
@RequestMapping("/test/emails")
public class TestEmailController {
    private final EmailMessageStore emailMessageStore;

    public TestEmailController(EmailMessageStore emailMessageStore) {
        this.emailMessageStore = emailMessageStore;
    }

    @GetMapping("/last")
    public ResponseEntity<EmailMessage> getLastEmail() {
        EmailMessage message = emailMessageStore.getLastMessage();
        if (message == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(message);
    }
}