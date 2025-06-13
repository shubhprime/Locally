package com.locally.backend.service.impl;

import com.locally.backend.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendVerificationOtp(String to, String otp) {
        String subject = "Email Verification OTP";
        String content = "<p>Dear user,</p>"
                + "<p>Your OTP for email verification is:</p>"
                + "<h2>" + otp + "</h2>"
                + "<p>This OTP will expire in 15 minutes.</p>"
                + "<p>If you did not request this, please ignore this email.</p>"
                + "<br><p>Thanks,<br>Locally Team</p>";

        sendEmail(to, subject, content);
    }

    @Override
    public void sendWelcomeEmail(String to, String name) {
        String subject = "Welcome to Locally!";
        String content = "Hello " + name + ",\n\nWelcome to Locally! We’re glad to have you on board.";

        sendEmail(to, subject, content);
    }

    private void sendEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, false);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}