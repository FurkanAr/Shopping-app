package org.commerce.authenticationservice.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

public class MailRequest {

    private String emailFrom;
    private String emailTo;
    private String subject;
    private String text;

    public MailRequest() {
    }

    public MailRequest(String emailFrom, String emailTo, String subject, String text) {
        this.emailFrom = emailFrom;
        this.emailTo = emailTo;
        this.subject = subject;
        this.text = text;
    }

    public String getEmailFrom() {
        return emailFrom;
    }

    public void setEmailFrom(String emailFrom) {
        this.emailFrom = emailFrom;
    }

    public String getEmailTo() {
        return emailTo;
    }

    public void setEmailTo(String emailTo) {
        this.emailTo = emailTo;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "MailRequest{" +
                "emailFrom='" + emailFrom + '\'' +
                ", emailTo='" + emailTo + '\'' +
                ", subject='" + subject + '\'' +
                '}';
    }
}