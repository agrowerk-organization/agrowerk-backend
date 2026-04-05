package tech.agrowerk.business.service.auth;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.infrastructure.exception.local.BadCredentialsException;
import tech.agrowerk.infrastructure.exception.local.InvalidTokenException;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.repository.core.UserRepository;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
public class PasswordResetService {

    private static final int TOKEN_EXPIRY_MINUTES = 15;
    private static final int COOLDOWN_MINUTES = 2;
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final int TOKEN_LENGTH = 8;

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public PasswordResetService(UserRepository userRepository,
                                JavaMailSender mailSender,
                                PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.mailSender      = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {

            if (user.getPasswordResetSentAt() != null) {
                Instant cooldownEnd = user.getPasswordResetSentAt().plus(COOLDOWN_MINUTES, ChronoUnit.MINUTES);
                if (Instant.now().isBefore(cooldownEnd)) {
                    log.warn("Reset password cooldown active for {}", email);
                    return;
                }
            }

            String token = generateToken();
            user.setPasswordResetToken(token);
            user.setPasswordResetSentAt(Instant.now());
            userRepository.save(user);

            sendResetEmail(user, token);
        });
    }

    @Transactional(readOnly = true)
    public void validateToken(String token) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new InvalidTokenException("Token inválido ou expirado"));

        checkExpiry(user);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new InvalidTokenException("Token inválido ou expirado"));

        checkExpiry(user);

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BadCredentialsException("A nova senha não pode ser igual à senha atual");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setLastPasswordChange(Instant.now());
        user.setPasswordResetToken(null);
        user.setPasswordResetSentAt(null);

        user.incrementTokenVersion();
        user.invalidateRefreshToken();

        userRepository.save(user);
        log.info("Senha redefinida com sucesso para {}", user.getEmail());
    }


    private void checkExpiry(User user) {
        Instant expiry = user.getPasswordResetSentAt().plus(TOKEN_EXPIRY_MINUTES, ChronoUnit.MINUTES);
        if (Instant.now().isAfter(expiry)) {
            throw new InvalidTokenException("Token expirado. Solicite um novo.");
        }
    }

    private String generateToken() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(TOKEN_LENGTH);
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    @Async
    void sendResetEmail(User user, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("AgroWerk — Redefinição de senha");
            helper.setText(buildEmailBody(user.getName(), token), true);

            mailSender.send(message);
            log.info("Email de redefinição enviado para {}", user.getEmail());
        } catch (Exception e) {
            log.error("Falha ao enviar email para {}: {}", user.getEmail(), e.getMessage());
        }
    }

    private String buildEmailBody(String name, String token) {
        return """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <body style="font-family: Arial, sans-serif; background-color: #0d140f; color: #ffffff; padding: 40px;">
              <div style="max-width: 520px; margin: 0 auto; background-color: #1a2e1c;
                          border-radius: 16px; padding: 40px; border: 1px solid #4CAF5040;">

                <img src="%s/assets/images/agrowerk.png" alt="AgroWerk"
                     style="height: 48px; margin-bottom: 32px;" />

                <h2 style="color: #4CAF50; margin-bottom: 8px;">Olá, %s!</h2>
                <p style="color: #ffffffaa; line-height: 1.6; margin-bottom: 24px;">
                  Recebemos uma solicitação para redefinir sua senha no
                  <strong style="color: #ffffff;">AgroWerk</strong>.
                  Use o código abaixo para continuar:
                </p>

                <div style="background-color: #0d140f; border: 1px solid #4CAF5060;
                            border-radius: 12px; padding: 20px 32px; text-align: center;
                            margin-bottom: 24px;">
                  <span style="font-size: 32px; font-weight: bold; letter-spacing: 8px;
                               color: #4CAF50; font-family: monospace;">%s</span>
                </div>

                <p style="color: #ffffffaa; font-size: 14px; line-height: 1.6;">
                  Este código expira em <strong style="color: #ffffff;">15 minutos</strong>.
                  Se você não solicitou a redefinição, ignore este e-mail — sua senha
                  permanece a mesma.
                </p>

                <hr style="border-color: #4CAF5020; margin: 24px 0;" />
                <p style="color: #ffffff33; font-size: 12px;">
                  AgroWerk · LGPD compliant · Seus dados estão protegidos
                </p>
              </div>
            </body>
            </html>
        """.formatted(baseUrl, name, token);
    }
}