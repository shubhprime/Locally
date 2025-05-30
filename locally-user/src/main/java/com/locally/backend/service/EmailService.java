//package com.locally.backend.service;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Profile;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//
//// TODO: Implement EmailService
//@Service
//@Profile("!no-mail")
//public class EmailService {
//
//    @Autowired
//    private JavaMailSender mailSender;
//
//    @Value("${spring.mail.username}")
//    private String fromEmail;
//
//    @Async
//    public void sendWelcomeEmail(String toEmail, String firstName) {
//        String emailBody = "Hello " + firstName + ",\n\n" +
//                "Welcome to Locally! We're excited to have you on board.\n\n" +
//                "Cheers,\nThe Locally Team";
//
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(toEmail);
//        message.setSubject("Welcome to Locally, " + firstName + "!");
//        message.setText(emailBody);
//        message.setFrom(fromEmail);
//
//        mailSender.send(message);
//    }
//}