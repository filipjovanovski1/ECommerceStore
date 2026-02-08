package com.mdtalalwasim.ecommerce.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail")
public class MailProperties {
    private String fromAddress;
    private String fromName;
    private String resetSubject;

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getResetSubject() {
        return resetSubject;
    }

    public void setResetSubject(String resetSubject) {
        this.resetSubject = resetSubject;
    }
}