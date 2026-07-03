package com.cm.matrimony_service.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.cm.matrimony_service.biodata.Biodata;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Mithila Matrimony - Email Verification");
            message.setText("Welcome to Mithila Matrimony!\n\nYour verification code is: " + otp + "\n\nThis code will expire in 5 minutes.\n\nThank you!");
            
            mailSender.send(message);
            log.info("OTP email sent successfully to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send OTP email. Please try again later.");
        }
    }

    public void sendRegistrationCompleteEmail(String toEmail, Biodata biodata) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Welcome to Mithila Matrimony - Profile Completed!");
            
            String text = "Congratulations " + biodata.getFullName() + "!\n\n"
                        + "Your Mithila Matrimony profile has been successfully created and completed.\n\n"
                        + "Here are the details we saved:\n"
                        + "- Full Name: " + biodata.getFullName() + "\n"
                        + "- Gender: " + (biodata.getGender() != null ? biodata.getGender().name() : "N/A") + "\n"
                        + "- Age: " + biodata.getAge() + "\n"
                        + "- Gotra: " + biodata.getGotra() + "\n"
                        + "- Profession: " + biodata.getProfession() + "\n"
                        + "- Location: " + biodata.getLocation() + "\n\n"
                        + "You can now log in using your email and password to start finding matches!\n\n"
                        + "Best Regards,\nThe Mithila Matrimony Team";
                        
            message.setText(text);
            mailSender.send(message);
            log.info("Registration completion email sent successfully to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send Registration Complete email to {}: {}", toEmail, e.getMessage(), e);
        }
    }
}
