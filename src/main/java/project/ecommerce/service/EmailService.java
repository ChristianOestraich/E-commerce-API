package project.ecommerce.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email enviado para: {}", to);

        } catch (MessagingException e) {
            log.error("Erro ao enviar email para {}: {}", to, e.getMessage());
        }
    }

    // Email de boas vindas apos registro
    @Async
    public void sendWelcomeEmail(String to, String name) {
        String subject = "Bem-vindo ao nosso E-commerce!";
        String content = """
                <html>
                <body style="font-family: Arial, sans-serif; color: #333;">
                    <div style="max-width: 600px; margin: auto; padding: 20px;">
                        <h2 style="color: #4CAF50;">Bem-vindo, %s!</h2>
                        <p>Sua conta foi criada com sucesso.</p>
                        <p>Agora voce pode explorar nosso catalogo e fazer seus pedidos.</p>
                        <br>
                        <p>Qualquer duvida, estamos a disposicao.</p>
                        <p><strong>Equipe E-commerce</strong></p>
                    </div>
                </body>
                </html>
                """.formatted(name);

        sendEmail(to, subject, content);
    }

    // Email de confirmacao de pedido
    @Async
    public void sendOrderConfirmationEmail(String to, String name,
                                           Long orderId, String total) {
        String subject = "Pedido #" + orderId + " recebido!";
        String content = """
                <html>
                <body style="font-family: Arial, sans-serif; color: #333;">
                    <div style="max-width: 600px; margin: auto; padding: 20px;">
                        <h2 style="color: #2196F3;">Pedido Recebido!</h2>
                        <p>Ola, <strong>%s</strong>!</p>
                        <p>Seu pedido <strong>#%d</strong> foi recebido com sucesso.</p>
                        <table style="width:100%%; border-collapse: collapse; margin-top: 10px;">
                            <tr style="background-color: #f2f2f2;">
                                <td style="padding: 8px; border: 1px solid #ddd;">
                                    <strong>Total</strong>
                                </td>
                                <td style="padding: 8px; border: 1px solid #ddd;">
                                    R$ %s
                                </td>
                            </tr>
                            <tr>
                                <td style="padding: 8px; border: 1px solid #ddd;">
                                    <strong>Status</strong>
                                </td>
                                <td style="padding: 8px; border: 1px solid #ddd;">
                                    Aguardando pagamento
                                </td>
                            </tr>
                        </table>
                        <br>
                        <p>Realize o pagamento para que seu pedido seja processado.</p>
                        <p><strong>Equipe E-commerce</strong></p>
                    </div>
                </body>
                </html>
                """.formatted(name, orderId, total);

        sendEmail(to, subject, content);
    }

    // Email de pagamento confirmado
    @Async
    public void sendPaymentConfirmedEmail(String to, String name,
                                          Long orderId, String total) {
        String subject = "Pagamento do Pedido #" + orderId + " confirmado!";
        String content = """
                <html>
                <body style="font-family: Arial, sans-serif; color: #333;">
                    <div style="max-width: 600px; margin: auto; padding: 20px;">
                        <h2 style="color: #4CAF50;">Pagamento Confirmado!</h2>
                        <p>Ola, <strong>%s</strong>!</p>
                        <p>O pagamento do seu pedido <strong>#%d</strong>
                           foi confirmado com sucesso.</p>
                        <table style="width:100%%; border-collapse: collapse; margin-top: 10px;">
                            <tr style="background-color: #f2f2f2;">
                                <td style="padding: 8px; border: 1px solid #ddd;">
                                    <strong>Total pago</strong>
                                </td>
                                <td style="padding: 8px; border: 1px solid #ddd;">
                                    R$ %s
                                </td>
                            </tr>
                            <tr>
                                <td style="padding: 8px; border: 1px solid #ddd;">
                                    <strong>Status</strong>
                                </td>
                                <td style="padding: 8px; border: 1px solid #ddd;">
                                    Confirmado
                                </td>
                            </tr>
                        </table>
                        <br>
                        <p>Seu pedido esta sendo preparado para envio.</p>
                        <p><strong>Equipe E-commerce</strong></p>
                    </div>
                </body>
                </html>
                """.formatted(name, orderId, total);

        sendEmail(to, subject, content);
    }

    // Email de pedido cancelado
    @Async
    public void sendOrderCancelledEmail(String to, String name, Long orderId) {
        String subject = "Pedido #" + orderId + " cancelado";
        String content = """
                <html>
                <body style="font-family: Arial, sans-serif; color: #333;">
                    <div style="max-width: 600px; margin: auto; padding: 20px;">
                        <h2 style="color: #f44336;">Pedido Cancelado</h2>
                        <p>Ola, <strong>%s</strong>!</p>
                        <p>Seu pedido <strong>#%d</strong> foi cancelado.</p>
                        <p>Se voce realizou algum pagamento, o estorno sera processado
                           em breve conforme a politica do meio de pagamento.</p>
                        <br>
                        <p>Em caso de duvidas, entre em contato conosco.</p>
                        <p><strong>Equipe E-commerce</strong></p>
                    </div>
                </body>
                </html>
                """.formatted(name, orderId);

        sendEmail(to, subject, content);
    }

    // Email de status do pedido atualizado
    @Async
    public void sendOrderStatusUpdatedEmail(String to, String name,
                                            Long orderId, String status) {
        String subject = "Atualizacao do Pedido #" + orderId;
        String content = """
                <html>
                <body style="font-family: Arial, sans-serif; color: #333;">
                    <div style="max-width: 600px; margin: auto; padding: 20px;">
                        <h2 style="color: #FF9800;">Pedido Atualizado</h2>
                        <p>Ola, <strong>%s</strong>!</p>
                        <p>O status do seu pedido <strong>#%d</strong>
                           foi atualizado para: <strong>%s</strong></p>
                        <br>
                        <p><strong>Equipe E-commerce</strong></p>
                    </div>
                </body>
                </html>
                """.formatted(name, orderId, status);

        sendEmail(to, subject, content);
    }
}