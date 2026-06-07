package com.example.userservice.producer;

import com.example.userservice.dto.EmailDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${broker.queue.email.name}")
    private String emailQueueName;

    public UserProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishEmailMessage(EmailDto emailDto) {
        rabbitTemplate.convertAndSend(emailQueueName, emailDto);
    }
}
