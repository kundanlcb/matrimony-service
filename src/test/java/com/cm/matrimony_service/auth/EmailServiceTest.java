package com.cm.matrimony_service.auth;

import com.cm.matrimony_service.biodata.Biodata;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private EmailService emailService;

    @Mock
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@maithilmatch.com");
    }

    @Test
    void testSendOtpEmail_Success() {
        // Arrange
        String toEmail = "user@example.com";
        String otp = "123456";
        String expectedHtml = "<html>OTP Email</html>";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("otp-email"), any(Context.class))).thenReturn(expectedHtml);

        // Act
        emailService.sendOtpEmail(toEmail, otp);

        // Assert
        verify(templateEngine).process(eq("otp-email"), any(Context.class));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void testSendRegistrationCompleteEmail_Success() {
        // Arrange
        String toEmail = "user@example.com";
        Biodata biodata = new Biodata();
        biodata.setFullName("Test User");
        biodata.setAge(25);
        String expectedHtml = "<html>Registration Complete</html>";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("registration-complete-email"), any(Context.class))).thenReturn(expectedHtml);

        // Act
        emailService.sendRegistrationCompleteEmail(toEmail, biodata);

        // Assert
        verify(templateEngine).process(eq("registration-complete-email"), any(Context.class));
        verify(mailSender).send(mimeMessage);
    }
}
