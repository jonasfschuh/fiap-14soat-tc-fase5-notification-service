package br.com.fiap.infrastructure.adapters.email;

import br.com.fiap.domain.exceptions.NotificationSendException;
import br.com.fiap.domain.model.VideoNotificationEvent;
import br.com.fiap.domain.ports.out.NotificationEmailPort;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class SmtpEmailAdapter implements NotificationEmailPort {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailAdapter.class);

    private final JavaMailSender mailSender;

    @Value("${app.notification.from-email:noreply@fiapx.com}")
    private String fromEmail;

    public SmtpEmailAdapter(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendFailureEmail(VideoNotificationEvent event) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(event.getUserEmail() != null ? event.getUserEmail() : "user@example.com");
            helper.setSubject("❌ Falha no processamento do vídeo: " + event.getOriginalFilename());
            String html = buildFailureEmailHtml(event);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("[EMAIL] Failure notification sent for videoId={}", event.getVideoId());
        } catch (Exception e) {
            log.error("[EMAIL] Failed to send failure email for videoId={}", event.getVideoId(), e);
            throw new NotificationSendException("Failed to send email: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendSuccessEmail(VideoNotificationEvent event) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(event.getUserEmail() != null ? event.getUserEmail() : "user@example.com");
            helper.setSubject("✅ Seu vídeo foi processado: " + event.getOriginalFilename());
            String html = buildSuccessEmailHtml(event);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("[EMAIL] Success notification sent for videoId={}", event.getVideoId());
        } catch (Exception e) {
            log.error("[EMAIL] Failed to send success email for videoId={}", event.getVideoId(), e);
            throw new NotificationSendException("Failed to send email: " + e.getMessage(), e);
        }
    }

    private String buildFailureEmailHtml(VideoNotificationEvent event) {
        return """
            <html><body style="font-family: Arial, sans-serif; padding: 20px;">
            <h2 style="color: #d32f2f;">❌ Falha no processamento do vídeo</h2>
            <p>Infelizmente, o processamento do seu vídeo falhou.</p>
            <table style="border-collapse: collapse; width: 100%%; max-width: 500px;">
              <tr><td style="padding: 8px; font-weight: bold;">Arquivo:</td><td style="padding: 8px;">%s</td></tr>
              <tr><td style="padding: 8px; font-weight: bold;">ID do Vídeo:</td><td style="padding: 8px;">%s</td></tr>
              <tr><td style="padding: 8px; font-weight: bold;">Data:</td><td style="padding: 8px;">%s</td></tr>
              <tr><td style="padding: 8px; font-weight: bold; color: #d32f2f;">Motivo:</td><td style="padding: 8px; color: #d32f2f;">%s</td></tr>
            </table>
            <p style="margin-top: 20px;">Por favor, faça upload novamente ou entre em contato com o suporte.</p>
            <p style="color: #666;">Equipe FIAP X</p>
            </body></html>
            """.formatted(
                event.getOriginalFilename(),
                event.getVideoId(),
                event.getTimestamp(),
                event.getErrorMessage() != null ? event.getErrorMessage() : "Erro desconhecido"
        );
    }

    private String buildSuccessEmailHtml(VideoNotificationEvent event) {
        return """
            <html><body style="font-family: Arial, sans-serif; padding: 20px;">
            <h2 style="color: #388e3c;">✅ Vídeo processado com sucesso!</h2>
            <p>O processamento do seu vídeo foi concluído com sucesso.</p>
            <table style="border-collapse: collapse; width: 100%%; max-width: 500px;">
              <tr><td style="padding: 8px; font-weight: bold;">Arquivo:</td><td style="padding: 8px;">%s</td></tr>
              <tr><td style="padding: 8px; font-weight: bold;">ID do Vídeo:</td><td style="padding: 8px;">%s</td></tr>
              <tr><td style="padding: 8px; font-weight: bold;">Data:</td><td style="padding: 8px;">%s</td></tr>
            </table>
            <p style="margin-top: 20px;">Acesse a plataforma para fazer o download dos frames processados.</p>
            <p style="color: #666;">Equipe FIAP X</p>
            </body></html>
            """.formatted(event.getOriginalFilename(), event.getVideoId(), event.getTimestamp());
    }
}
