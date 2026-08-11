package com.espay.admincore.adapter.out.mail;

import com.espay.admincore.application.dto.auth.SendOtpBarcodeEmailCommand;
import com.espay.admincore.config.OtpProperties;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class SmtpOtpMailAdapterTest {

    @Test
    void OTP_QR_등록_안내_HTML_메일을_생성한다() throws Exception {
        CapturingMailSender mailSender = new CapturingMailSender();
        mailSender.setUsername("sender@example.com");
        SmtpOtpMailAdapter adapter = new SmtpOtpMailAdapter(
                mailSender,
                new OtpProperties("ADMIN-CORE", "admin-core", 250, 5)
        );

        adapter.sendOtpBarcodeEmail(SendOtpBarcodeEmailCommand.of(
                "admin@example.com",
                "<초기 관리자>",
                Base64.getEncoder().encodeToString("png".getBytes())
        ));

        MimeMessage message = mailSender.sentMessage;
        String html = findHtmlBody(message);

        assertThat(message.getSubject()).isEqualTo("ADMIN-CORE 관리자 OTP QR 등록");
        assertThat(html)
                .contains("ADMIN-CORE Admin")
                .contains("안녕하세요 &lt;초기 관리자&gt;님,")
                .contains("등록 방법")
                .contains("보안 안내")
                .contains("src=\"cid:otpQr\"")
                .contains("width=\"250\"")
                .doesNotContain("안녕하세요 <초기 관리자>님,");
        assertThat(containsInlineQr(message)).isTrue();
    }

    private static String findHtmlBody(Part part) throws Exception {
        if (part.isMimeType("text/html")) {
            return part.getContent().toString();
        }
        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            for (int index = 0; index < multipart.getCount(); index++) {
                String html = findHtmlBody(multipart.getBodyPart(index));
                if (html != null) {
                    return html;
                }
            }
        }
        return null;
    }

    private static boolean containsInlineQr(Part part) throws Exception {
        String[] contentIds = part.getHeader("Content-ID");
        if (contentIds != null && contentIds.length > 0 && "<otpQr>".equals(contentIds[0])) {
            return true;
        }
        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            for (int index = 0; index < multipart.getCount(); index++) {
                if (containsInlineQr(multipart.getBodyPart(index))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static final class CapturingMailSender extends JavaMailSenderImpl {
        private MimeMessage sentMessage;

        @Override
        public void send(MimeMessage... mimeMessages) {
            sentMessage = mimeMessages[0];
            try {
                sentMessage.saveChanges();
            } catch (Exception exception) {
                throw new IllegalStateException(exception);
            }
        }
    }
}
