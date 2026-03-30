package project.ecommerce.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    // ── Estilos base compartilhados ────────────────────────
    private static final String BASE_STYLE = """
        <html>
        <head>
          <meta charset="UTF-8"/>
          <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
        </head>
        <body style="margin:0;padding:0;background-color:#f4f6f0;font-family:'Segoe UI',Arial,sans-serif;">
          <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f6f0;padding:32px 16px;">
            <tr><td align="center">
              <table width="600" cellpadding="0" cellspacing="0" style="max-width:600px;width:100%%;">
        """;

    private static final String BASE_CLOSE = """
              </table>
            </td></tr>
          </table>
        </body>
        </html>
        """;

    private String header(String emoji, String title, String subtitle, String headerColor) {
        return """
            <tr>
              <td style="background:linear-gradient(135deg,%s,#1b5e20);border-radius:12px 12px 0 0;padding:36px 40px;text-align:center;">
                <div style="font-size:52px;margin-bottom:12px;">%s</div>
                <h1 style="color:white;margin:0;font-size:26px;font-weight:900;letter-spacing:1px;">%s</h1>
                <p style="color:rgba(255,255,255,0.8);margin:8px 0 0;font-size:15px;">%s</p>
              </td>
            </tr>
            """.formatted(headerColor, emoji, title, subtitle);
    }

    private String footer() {
        return """
            <tr>
              <td style="background:linear-gradient(135deg,#1a4a1e,#1b5e20);border-radius:0 0 12px 12px;padding:28px 40px;text-align:center;">
                <div style="margin-bottom:12px;">
                  <span style="background:linear-gradient(135deg,#e65100,#ff8f00);color:white;font-weight:900;
                    font-size:18px;letter-spacing:3px;padding:6px 16px;border-radius:6px;">AGROVET</span>
                </div>
                <p style="color:rgba(255,255,255,0.6);font-size:12px;margin:4px 0;">Campo &amp; Saude Animal</p>
                <p style="color:rgba(255,255,255,0.4);font-size:11px;margin:8px 0 0;">
                  (51) 99999-9999 &nbsp;|&nbsp; contato@agrovet.com.br
                </p>
                <p style="color:rgba(255,255,255,0.3);font-size:10px;margin:12px 0 0;">
                  Este e um email automatico, por favor nao responda.
                </p>
              </td>
            </tr>
            """;
    }

    private String body(String content) {
        return """
            <tr>
              <td style="background:white;padding:36px 40px;">
                %s
              </td>
            </tr>
            """.formatted(content);
    }

    private String greeting(String name) {
        return "<p style='color:#333;font-size:16px;margin:0 0 16px;'>Ola, <strong style='color:#1b5e20;'>%s</strong>!</p>"
                .formatted(name);
    }

    private String orderBadge(Long orderId) {
        return """
            <div style="text-align:center;margin:20px 0;">
              <span style="background:#e8f5e9;color:#1b5e20;font-size:22px;font-weight:900;
                padding:10px 24px;border-radius:8px;border:2px solid #a5d6a7;letter-spacing:1px;">
                Pedido #%d
              </span>
            </div>
            """.formatted(orderId);
    }

    private String statusChip(String label, String color, String bg) {
        return "<span style='background:%s;color:%s;font-weight:700;font-size:13px;padding:4px 14px;border-radius:20px;'>%s</span>"
                .formatted(bg, color, label);
    }

    private String infoRow(String label, String value, boolean gray) {
        String bg = gray ? "#f9f9f9" : "white";
        return """
            <tr style="background:%s;">
              <td style="padding:12px 16px;color:#757575;font-size:14px;border-bottom:1px solid #f0f0f0;width:40%%;">%s</td>
              <td style="padding:12px 16px;color:#212121;font-size:14px;font-weight:600;border-bottom:1px solid #f0f0f0;">%s</td>
            </tr>
            """.formatted(bg, label, value);
    }

    private String table(String rows) {
        return """
            <table width="100%%" cellpadding="0" cellspacing="0"
              style="border:1px solid #e0e0e0;border-radius:8px;overflow:hidden;margin:20px 0;">
              %s
            </table>
            """.formatted(rows);
    }

    private String ctaButton(String text, String url, String color) {
        return """
            <div style="text-align:center;margin:28px 0 8px;">
              <a href="%s" style="background:linear-gradient(135deg,%s);color:white;font-weight:800;
                font-size:15px;padding:14px 36px;border-radius:8px;text-decoration:none;
                display:inline-block;box-shadow:0 4px 16px rgba(0,0,0,0.2);">
                %s
              </a>
            </div>
            """.formatted(url, color, text);
    }

    // ── Envio generico ─────────────────────────────────────
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

    // ── Boas-vindas ────────────────────────────────────────
    @Async
    public void sendWelcomeEmail(String to, String name) {
        String subject = "🌱 Bem-vindo ao AgroVet, " + name + "!";

        String content = BASE_STYLE
                + header("🌱", "Bem-vindo ao AgroVet!", "Sua conta foi criada com sucesso", "#2e7d32")
                + body(greeting(name) + """
                <p style="color:#555;font-size:15px;line-height:1.7;margin:0 0 24px;">
                  Estamos muito felizes em ter voce como cliente! Agora voce tem acesso a
                  todos os nossos produtos para o campo e saude animal com os melhores precos.
                </p>
                <table width="100%%" cellpadding="0" cellspacing="0" style="margin:20px 0;">
                  <tr>
                    <td style="background:#e8f5e9;border-radius:8px;padding:16px;border-left:4px solid #2e7d32;">
                      <p style="margin:0;color:#1b5e20;font-size:14px;font-weight:700;">✅ O que voce pode fazer agora:</p>
                      <ul style="color:#555;font-size:14px;margin:8px 0 0;padding-left:20px;line-height:2;">
                        <li>Explorar nosso catalogo de produtos</li>
                        <li>Adicionar produtos aos favoritos</li>
                        <li>Realizar seu primeiro pedido</li>
                        <li>Gerenciar seus enderecos de entrega</li>
                      </ul>
                    </td>
                  </tr>
                </table>
                """ + ctaButton("🛒 Explorar Produtos", "http://localhost:3000/products", "#e65100,#ff8f00"))
                + footer()
                + BASE_CLOSE;

        sendEmail(to, subject, content);
    }

    // ── Confirmacao de pedido ──────────────────────────────
    @Async
    public void sendOrderConfirmationEmail(String to, String name, Long orderId, String total) {
        String subject = "✅ Pedido #" + orderId + " recebido com sucesso!";

        String content = BASE_STYLE
                + header("✅", "Pedido Recebido!", "Seu pedido foi registrado e esta sendo processado", "#2e7d32")
                + body(greeting(name)
                + "<p style='color:#555;font-size:15px;line-height:1.7;margin:0 0 8px;'>Recebemos seu pedido e ja estamos cuidando dele. Confira os detalhes abaixo:</p>"
                + orderBadge(orderId)
                + table(
                infoRow("Status", statusChip("Aguardando Pagamento", "#ff8f00", "#fff8e1"), true)
                        + infoRow("Total", "<strong style='color:#2e7d32;font-size:16px;'>R$ " + total + "</strong>", false)
                        + infoRow("Proximo passo", "Realize o pagamento para confirmar", true)
        )
                + "<p style='color:#888;font-size:13px;margin:16px 0 0;text-align:center;'>"
                + "Voce tambem pode acompanhar o status do seu pedido na area <strong>Meus Pedidos</strong>.</p>"
                + ctaButton("📦 Ver Meus Pedidos", "http://localhost:3000/my-orders", "#2e7d32,#4caf50"))
                + footer()
                + BASE_CLOSE;

        sendEmail(to, subject, content);
    }

    // ── Pagamento confirmado ───────────────────────────────
    @Async
    public void sendPaymentConfirmedEmail(String to, String name, Long orderId, String total) {
        String subject = "💳 Pagamento do Pedido #" + orderId + " confirmado!";

        String content = BASE_STYLE
                + header("💳", "Pagamento Confirmado!", "Seu pagamento foi processado com sucesso", "#1565c0")
                + body(greeting(name)
                + "<p style='color:#555;font-size:15px;line-height:1.7;margin:0 0 8px;'>Otima noticia! O pagamento do seu pedido foi confirmado e ja estamos preparando tudo para o envio.</p>"
                + orderBadge(orderId)
                + table(
                infoRow("Status do pagamento", statusChip("Pago", "#2e7d32", "#e8f5e9"), true)
                        + infoRow("Total pago", "<strong style='color:#2e7d32;font-size:16px;'>R$ " + total + "</strong>", false)
                        + infoRow("Proximo passo", "Seu pedido sera preparado para envio", true)
        )
                + """
                  <div style="background:#e3f2fd;border-radius:8px;padding:16px;margin:20px 0;border-left:4px solid #1565c0;">
                    <p style="margin:0;color:#1565c0;font-size:14px;">
                      🚚 <strong>Envio em breve!</strong> Assim que seu pedido for despachado voce receberá uma notificação.
                    </p>
                  </div>
                  """
                + ctaButton("📦 Acompanhar Pedido", "http://localhost:3000/my-orders", "#1565c0,#1976d2"))
                + footer()
                + BASE_CLOSE;

        sendEmail(to, subject, content);
    }

    // ── Pedido cancelado ───────────────────────────────────
    @Async
    public void sendOrderCancelledEmail(String to, String name, Long orderId) {
        String subject = "❌ Pedido #" + orderId + " cancelado";

        String content = BASE_STYLE
                + header("❌", "Pedido Cancelado", "Seu pedido foi cancelado conforme solicitado", "#c62828")
                + body(greeting(name)
                + "<p style='color:#555;font-size:15px;line-height:1.7;margin:0 0 8px;'>Confirmamos o cancelamento do seu pedido:</p>"
                + orderBadge(orderId)
                + table(
                infoRow("Status", statusChip("Cancelado", "#e53935", "#ffebee"), true)
                        + infoRow("Estoque", "Os produtos foram devolvidos ao estoque", false)
                        + infoRow("Reembolso", "Sera processado em ate 5 dias uteis", true)
        )
                + """
                  <div style="background:#fff8e1;border-radius:8px;padding:16px;margin:20px 0;border-left:4px solid #ff8f00;">
                    <p style="margin:0;color:#e65100;font-size:14px;">
                      ℹ️ Se voce realizou algum pagamento, o estorno sera processado automaticamente
                      conforme a politica do meio de pagamento utilizado.
                    </p>
                  </div>
                  <p style="color:#555;font-size:14px;margin:16px 0 0;">
                    Mudou de ideia? Nossos produtos continuam disponíveis para uma nova compra!
                  </p>
                  """
                + ctaButton("🛒 Explorar Produtos", "http://localhost:3000/products", "#e65100,#ff8f00"))
                + footer()
                + BASE_CLOSE;

        sendEmail(to, subject, content);
    }

    // ── Status atualizado ──────────────────────────────────
    @Async
    public void sendOrderStatusUpdatedEmail(String to, String name, Long orderId, String status) {
        String[] cfg = switch (status) {
            case "CONFIRMED" -> new String[]{"✅", "Pedido Confirmado!", "Seu pedido foi confirmado", "#2e7d32", "Confirmado", "#2e7d32", "#e8f5e9"};
            case "SHIPPED"   -> new String[]{"🚚", "Pedido Enviado!", "Seu pedido esta a caminho", "#6a1b9a", "Enviado", "#6a1b9a", "#f3e5f5"};
            case "DELIVERED" -> new String[]{"🎉", "Pedido Entregue!", "Seu pedido foi entregue com sucesso", "#1b5e20", "Entregue", "#2e7d32", "#e8f5e9"};
            default          -> new String[]{"📋", "Status Atualizado", "O status do seu pedido foi atualizado", "#1565c0", status, "#1565c0", "#e3f2fd"};
        };

        String emoji = cfg[0], title = cfg[1], subtitle = cfg[2], headerColor = cfg[3];
        String statusLabel = cfg[4], statusColor = cfg[5], statusBg = cfg[6];

        String extraMsg = switch (status) {
            case "CONFIRMED" -> "<p style='color:#555;font-size:15px;'>Seu pedido foi confirmado e esta sendo preparado com cuidado.</p>";
            case "SHIPPED"   -> "<p style='color:#555;font-size:15px;'>Seu pedido saiu para entrega! Em breve estara em suas maos.</p>";
            case "DELIVERED" -> "<p style='color:#555;font-size:15px;'>Esperamos que voce esteja satisfeito com sua compra. Aproveite!</p>";
            default          -> "<p style='color:#555;font-size:15px;'>O status do seu pedido foi atualizado. Acompanhe na area Meus Pedidos.</p>";
        };

        String content = BASE_STYLE
                + header(emoji, title, subtitle, headerColor)
                + body(greeting(name)
                + extraMsg
                + orderBadge(orderId)
                + table(infoRow("Novo status", statusChip(statusLabel, statusColor, statusBg), true))
                + (status.equals("DELIVERED") ? """
                  <div style="background:#e8f5e9;border-radius:8px;padding:16px;margin:20px 0;border-left:4px solid #2e7d32;text-align:center;">
                    <p style="margin:0;color:#1b5e20;font-size:15px;font-weight:700;">
                      ⭐ Gostou do produto? Deixe sua avaliacao!
                    </p>
                    <p style="margin:6px 0 0;color:#555;font-size:13px;">Sua opiniao ajuda outros clientes a escolher melhor.</p>
                  </div>
                  """ : "")
                + ctaButton("📦 Ver Meu Pedido", "http://localhost:3000/my-orders", headerColor + ",#2e7d32"))
                + footer()
                + BASE_CLOSE;

        String subject = emoji + " Pedido #" + orderId + " — " + title;
        sendEmail(to, subject, content);
    }

    // ── Queda de preco ─────────────────────────────────────
    @Async
    public void sendPriceDropEmail(String to, String name, String productName, BigDecimal newPrice) {
        String subject = "🔥 Queda de preco: " + productName;

        String content = BASE_STYLE
                + header("🔥", "Promocao Especial!", "Um produto da sua lista de desejos esta em oferta", "#e65100")
                + body(greeting(name)
                + "<p style='color:#555;font-size:15px;line-height:1.7;margin:0 0 8px;'>Uma boa noticia: o preco de um produto que voce salvou nos favoritos acabou de cair!</p>"
                + table(
                infoRow("Produto", "<strong>" + productName + "</strong>", true)
                        + infoRow("Novo preco", "<strong style='color:#e65100;font-size:18px;'>R$ " + newPrice.toPlainString() + "</strong>", false)
        )
                + """
                  <div style="background:#fff3e0;border-radius:8px;padding:16px;margin:20px 0;border-left:4px solid #e65100;text-align:center;">
                    <p style="margin:0;color:#e65100;font-size:15px;font-weight:700;">
                      ⚡ Oferta por tempo limitado — corra antes que acabe!
                    </p>
                  </div>
                  """
                + ctaButton("🛒 Comprar Agora", "http://localhost:3000/products", "#e65100,#ff8f00"))
                + footer()
                + BASE_CLOSE;

        sendEmail(to, subject, content);
    }
}