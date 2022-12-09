package com.conwine.server.notification;

import com.conwine.server.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailNotificationService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String from;

    public void sendActivationEmail(User user) {

        SimpleMailMessage sms = new SimpleMailMessage();
        sms.setTo(user.getEmail());
        sms.setFrom(from);
        sms.setReplyTo(from);
        sms.setSubject("Ti sei iscritto a Conwine");
        sms.setText("Il codice di attivazione è:" + user.getActivationCode());
        mailSender.send(sms);

    }
}
