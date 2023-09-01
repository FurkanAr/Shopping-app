package org.commerce.listener;

import org.commerce.request.MailRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    Logger logger = LoggerFactory.getLogger(getClass());


    @RabbitListener(queues = "${spcloud.mail.queue.name}")
    public void notificationListener(MailRequest mailRequest){
        logger.info("Notification listener invoked - Consuming Message with EmailRequest Email: {}",
                mailRequest.getEmailTo());

        logger.info("mailRequest: {}", mailRequest);

        logger.info("notificationListener method successfully worked");
    }
}
