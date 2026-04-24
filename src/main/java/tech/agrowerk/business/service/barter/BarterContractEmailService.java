package tech.agrowerk.business.service.barter;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tech.agrowerk.infrastructure.model.barter.BarterContract;
import tech.agrowerk.infrastructure.model.core.User;

@Service
@Slf4j
public class BarterContractEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    public BarterContractEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendContractToParties(BarterContract contract, byte[] pdfBytes) {
        User offeror  = contract.getTransaction().getOfferor();
        User acceptor = contract.getTransaction().getAcceptor();

        sendTo(offeror,  contract, pdfBytes, "Ofertante");
        sendTo(acceptor, contract, pdfBytes, "Aceitante");
    }

    private void sendTo(User user, BarterContract contract,
                        byte[] pdfBytes, String role) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("AgroWerk — Contrato de Barter Nº " + contract.getContractNumber());
            helper.setText(buildBody(user.getName(), role, contract), true);

            helper.addAttachment(
                    "contrato-barter-" + contract.getContractNumber() + ".pdf",
                    new org.springframework.core.io.ByteArrayResource(pdfBytes),
                    "application/pdf"
            );

            mailSender.send(message);
            log.info("Contrato enviado para {} ({}) contract={}",
                    user.getEmail(), role, contract.getContractNumber());

        } catch (Exception e) {
            log.error("Falha ao enviar contrato para {} contract={}: {}",
                    user.getEmail(), contract.getContractNumber(), e.getMessage());
        }
    }

    private String buildBody(String name, String role, BarterContract contract) {
        return """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <body style="font-family: Arial, sans-serif; background-color: #0d140f;
                         color: #ffffff; padding: 40px;">
              <div style="max-width: 520px; margin: 0 auto; background-color: #1a2e1c;
                          border-radius: 16px; padding: 40px; border: 1px solid #4CAF5040;">

                <div style="color: #4CAF50; font-size: 20pt; font-weight: bold;
                            margin-bottom: 24px;">AgroWerk</div>

                <h2 style="color: #4CAF50; margin-bottom: 8px;">
                  Contrato de Barter gerado!
                </h2>

                <p style="color: #ffffffaa; line-height: 1.6;">
                  Olá, <strong style="color:#fff;">%s</strong>.<br/>
                  Sua negociação de barter foi confirmada. O contrato
                  <strong style="color:#fff;">Nº %s</strong> foi gerado e
                  está anexado a este e-mail.
                </p>

                <div style="background-color:#0d200f; border-radius:8px;
                            padding:16px; margin: 20px 0; font-size:13px;">
                  <div style="color:#4CAF50; margin-bottom:4px;">Sua posição</div>
                  <div style="color:#fff; font-size:15px;">%s</div>
                  <div style="color:#aaa; margin-top:8px;">
                    Vigência: %s · Contrato precisa de assinatura de ambas as partes.
                  </div>
                </div>

                <p style="color: #ffffff55; font-size: 12px; margin-top: 24px;">
                  AgroWerk · LGPD compliant · Dados protegidos
                </p>
              </div>
            </body>
            </html>
        """.formatted(
                name,
                contract.getContractNumber(),
                role,
                contract.getEndDate()
        );
    }
}