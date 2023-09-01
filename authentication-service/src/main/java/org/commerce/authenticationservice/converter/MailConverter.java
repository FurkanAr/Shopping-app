package org.commerce.authenticationservice.converter;

import org.commerce.authenticationservice.request.MailRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MailConverter {

    @Value("${spcloud.mail.company}")
    private String COMPANY_MAIL;
    Logger logger = LoggerFactory.getLogger(getClass());

    public MailRequest convert(String email, String subject, String text){
        logger.info("convert to MailRequest method started");
        MailRequest mailRequest = new MailRequest();
        mailRequest.setEmailTo(email);
        mailRequest.setEmailFrom(COMPANY_MAIL);
        mailRequest.setSubject(subject);
        mailRequest.setText(text);
        logger.info("convert to MailRequest method successfully worked");
        return mailRequest;
    }
}
