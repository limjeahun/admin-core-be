package com.espay.admincore.adapter.out.mail;

import com.espay.admincore.application.dto.auth.SendOtpBarcodeEmailCommand;
import com.espay.admincore.application.port.out.auth.OtpMailPort;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * SMTP를 이용해 OTP 등록 QR 이미지를 인라인 첨부한 HTML 메일을 전송하는 어댑터.
 */
@Component
@RequiredArgsConstructor
public class SmtpOtpMailAdapter implements OtpMailPort {
    private final JavaMailSenderImpl mailSender;

    /**
     * Base64 PNG를 디코딩해 {@code cid:otpQr} 인라인 이미지로 포함한 안내 메일을 전송한다.
     *
     * @param command 수신자와 Base64 PNG QR 이미지를 묶은 발송 명령
     * @throws IllegalStateException 메시지 작성 또는 SMTP 전송에 실패한 경우
     */
    @Override
    public void sendOtpBarcodeEmail(SendOtpBarcodeEmailCommand command) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(mailSender.getUsername());
            helper.setTo(command.email());
            helper.setSubject("관리자 OTP 등록 안내");
            helper.setText("<p>" + command.userName()
                    + "님의 OTP 등록용 QR 코드입니다.</p><img src='cid:otpQr'/>", true);
            helper.addInline("otpQr", new ByteArrayResource(
                    Base64.getDecoder().decode(command.qrCodeBase64())), "image/png");
            mailSender.send(message);
        } catch (Exception exception) {
            throw new IllegalStateException("OTP 등록 메일 발송에 실패했습니다.", exception);
        }
    }
}
