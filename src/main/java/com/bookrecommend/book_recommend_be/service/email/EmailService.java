package com.bookrecommend.book_recommend_be.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService implements IEmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Override
    public void sendVerificationEmail(String to, String username, String verificationLink) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("verificationLink", verificationLink);

            String htmlContent = templateEngine.process("email_verification", context);

            sendHtmlEmail(to, "Verify your email address", htmlContent);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    @Override
    public void sendResetPasswordEmail(String to, String username, String resetLink) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("resetLink", resetLink);

            String htmlContent = templateEngine.process("email_reset_password", context);

            sendHtmlEmail(to, "Reset your password", htmlContent);
        } catch (Exception e) {
            log.error("Failed to send reset password email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send reset password email", e);
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        mailSender.send(message);
    }
}