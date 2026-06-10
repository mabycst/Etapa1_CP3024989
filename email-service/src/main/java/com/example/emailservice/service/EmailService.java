package com.example.emailservice.service;

import com.example.emailservice.domain.EmailModel;
import com.example.emailservice.domain.EmailStatus;
import com.example.emailservice.dto.EmailRecordDto;
import com.example.emailservice.repository.EmailRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class EmailService {

    private final EmailRepository emailRepository;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String emailFrom;

    public EmailService(EmailRepository emailRepository, JavaMailSender mailSender) {
        this.emailRepository = emailRepository;
        this.mailSender = mailSender;
    }

    @Transactional
    public EmailModel sendEmail(EmailRecordDto emailRecordDto) {
        EmailModel emailModel = new EmailModel();
        emailModel.setUserId(emailRecordDto.userId());
        emailModel.setEmailFrom(emailFrom);
        emailModel.setEmailTo(emailRecordDto.emailTo());
        emailModel.setSubject(emailRecordDto.subject());
        emailModel.setText(emailRecordDto.text());
        emailModel.setSendDateEmail(LocalDateTime.now());

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailFrom);
            message.setTo(emailRecordDto.emailTo());
            message.setSubject(emailRecordDto.subject());
            message.setText(emailRecordDto.text());

            mailSender.send(message);
            emailModel.setStatus(EmailStatus.SENT);
            System.out.println("E-mail enviado com sucesso para: " + emailRecordDto.emailTo());
        } catch (MailException e) {
            emailModel.setStatus(EmailStatus.ERROR);
            System.err.println("Erro ao enviar e-mail para: " + emailRecordDto.emailTo() + ". Erro: " + e.getMessage());
        }

        return emailRepository.save(emailModel);
    }
}
