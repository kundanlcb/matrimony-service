package com.cm.matrimony_service.auth;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.cm.matrimony_service.biodata.Biodata;

/**
 * Service responsible for sending out emails, such as OTPs and registration completions.
 * Uses Thymeleaf templates to render HTML emails.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    /**
     * Sends an OTP email for verification during registration or password reset.
     *
     * @param toEmail The recipient's email address
     * @param otp     The One-Time Password to send
     */
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, "Maithil Match");
            helper.setTo(toEmail);
            helper.setReplyTo(fromEmail, "Maithil Match Support");
            helper.setSubject("Maithil Match - Email Verification");
            
            Context context = new Context();
            context.setVariable("otp", otp);
            
            String htmlContent = templateEngine.process("otp-email", context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("OTP HTML email sent successfully to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP HTML email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send OTP email. Please try again later.");
        }
    }

    /**
     * Sends a welcome email upon successful registration and profile completion.
     *
     * @param toEmail The recipient's email address
     * @param biodata The user's completed biodata information to display in the email
     */
    public void sendRegistrationCompleteEmail(String toEmail, Biodata biodata) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, "Maithil Match");
            helper.setTo(toEmail);
            helper.setReplyTo(fromEmail, "Maithil Match Support");
            helper.setSubject("Welcome to Maithil Match - Profile Completed!");
            
            Context context = new Context();
            context.setVariable("fullName", biodata.getFullName());
            context.setVariable("gender", biodata.getGender() != null ? biodata.getGender().name() : "N/A");
            context.setVariable("age", biodata.getAge());
            context.setVariable("gotra", biodata.getGotra());
            context.setVariable("profession", biodata.getProfession());
            context.setVariable("location", biodata.getLocation());
            
            String htmlContent = templateEngine.process("registration-complete-email", context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Registration completion HTML email sent successfully to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send Registration Complete HTML email to {}: {}", toEmail, e.getMessage(), e);
        }
    }
}
