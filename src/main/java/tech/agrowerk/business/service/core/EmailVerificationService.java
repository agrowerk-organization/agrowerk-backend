package tech.agrowerk.business.service.core;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.exception.local.InvalidTokenException;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.repository.core.UserRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@Slf4j
public class EmailVerificationService {

    private static final int TOKEN_EXPIRY_HOURS = 24;

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailVerificationService(JavaMailSender mailSender, UserRepository userRepository) {
        this.mailSender = mailSender;
        this.userRepository = userRepository;
    }

    @Async
    public void sendVerificationEmail(User user) {
        String token = UUID.randomUUID().toString();

        user.setEmailVerificationToken(token);
        user.setEmailVerificationSentAt(Instant.now());
        userRepository.save(user);

        String verificationUrl = baseUrl + "/auth/verify-email?token=" + token;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("AgroWerk — Confirme seu e-mail");
            helper.setText(buildEmailBody(user.getName(), verificationUrl), true);

            mailSender.send(message);
            log.info("Verification email sent to {}", user.getEmail());

        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired verification token"));

        Instant expiry = user.getEmailVerificationSentAt()
                .plus(TOKEN_EXPIRY_HOURS, ChronoUnit.HOURS);

        if (Instant.now().isAfter(expiry)) {
            throw new InvalidTokenException("Verification token has expired. Please request a new one.");
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationSentAt(null);
        userRepository.save(user);

        log.info("Email verified for user {}", user.getEmail());
    }

    @Async
    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (user.isEmailVerified()) {
            log.warn("Resend requested but email already verified for {}", email);
            return;
        }

        if (user.getEmailVerificationSentAt() != null) {
            Instant cooldown = user.getEmailVerificationSentAt().plus(2, ChronoUnit.MINUTES);
            if (Instant.now().isBefore(cooldown)) {
                log.warn("Resend throttled for {}", email);
                return;
            }
        }

        sendVerificationEmail(user);
    }

    private String buildEmailBody(String name, String verificationUrl) {
        return """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <body style="font-family: Arial, sans-serif; background-color: #0d140f; color: #ffffff; padding: 40px;">
              <div style="max-width: 520px; margin: 0 auto; background-color: #1a2e1c;
                          border-radius: 16px; padding: 40px; border: 1px solid #4CAF5040;">
 
                <img src="%s/assets/images/agrowerk.png" alt="AgroWerk"
                     style="height: 48px; margin-bottom: 32px;" />
 
                <h2 style="color: #4CAF50; margin-bottom: 8px;">
                  Olá, %s!
                </h2>
                <p style="color: #ffffffaa; line-height: 1.6; margin-bottom: 24px;">
                  Obrigado por se cadastrar no <strong style="color: #ffffff;">AgroWerk</strong>.
                  Confirme seu e-mail clicando no botão abaixo para ativar sua conta.
                </p>
 
                <a href="%s"
                   style="display: inline-block; background-color: #4CAF50; color: #ffffff;
                          text-decoration: none; padding: 14px 32px; border-radius: 10px;
                          font-weight: bold; font-size: 16px; margin-bottom: 24px;">
                  Confirmar e-mail
                </a>
 
                <p style="color: #ffffff55; font-size: 13px; margin-top: 24px;">
                  Este link expira em 24 horas. Se você não criou uma conta, ignore este e-mail.
                </p>
 
                <hr style="border-color: #4CAF5020; margin: 24px 0;" />
                <p style="color: #ffffff33; font-size: 12px;">
                  AgroWerk · LGPD compliant · Seus dados estão protegidos
                </p>
              </div>
            </body>
            </html>
        """.formatted(baseUrl, name, verificationUrl);
    }
}
