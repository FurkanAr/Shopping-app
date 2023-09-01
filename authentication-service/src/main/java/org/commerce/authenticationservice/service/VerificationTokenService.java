package org.commerce.authenticationservice.service;

import org.commerce.authenticationservice.exception.messages.Messages;
import org.commerce.authenticationservice.exception.token.VerificationTokenNotFoundException;
import org.commerce.authenticationservice.model.User;
import org.commerce.authenticationservice.model.VerificationToken;
import org.commerce.authenticationservice.repository.VerificationTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class VerificationTokenService {
    private final VerificationTokenRepository verificationTokenRepository;
    Logger logger = LoggerFactory.getLogger(getClass());


    public VerificationTokenService(VerificationTokenRepository verificationTokenRepository) {
        this.verificationTokenRepository = verificationTokenRepository;
    }

    @Transactional
    protected void createVerificationToken(String token, User user) {
        logger.info("saveVerificationToken method started");
        VerificationToken verificationToken = new VerificationToken(token,
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(15), user);
        verificationTokenRepository.save(verificationToken);
        logger.info("saveVerificationToken method successfully worked");
    }

    public VerificationToken getToken(String token) {
        logger.info("getToken method started");
         VerificationToken verificationToken = verificationTokenRepository.findByToken(token).orElseThrow(() ->
                new VerificationTokenNotFoundException(Messages.VerificationToken.NOT_EXISTS));
        logger.info("getToken method successfully worked");
        return verificationToken;
    }

    @Transactional
    public void deleteToken(String token) {
        VerificationToken verificationToken = getToken(token);
        verificationTokenRepository.delete(verificationToken);
    }

    @Transactional
    public void setEnableToken(String token) {
        VerificationToken verificationToken = getToken(token);
        verificationToken.setConfirmedAt(LocalDateTime.now());
        verificationTokenRepository.save(verificationToken);
    }

}
