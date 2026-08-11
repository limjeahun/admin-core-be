package com.espay.admincore.adapter.out.mail;

import com.espay.admincore.application.dto.auth.SendOtpBarcodeEmailCommand;
import com.espay.admincore.application.port.out.auth.OtpMailPort;
import com.espay.admincore.config.OtpProperties;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

/**
 * SMTP를 이용해 OTP 등록 QR 이미지를 인라인 첨부한 HTML 메일을 전송하는 어댑터.
 */
@Component
@RequiredArgsConstructor
public class SmtpOtpMailAdapter implements OtpMailPort {
    private final JavaMailSenderImpl mailSender;
    private final OtpProperties      otpProperties;

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
            helper.setFrom(Objects.requireNonNull(mailSender.getUsername()));
            helper.setTo(command.email());
            helper.setSubject(createSubject());
            helper.setText(createHtmlBody(command.userName()), true);
            helper.addInline(
                    "otpQr",
                    new ByteArrayResource(Base64.getDecoder().decode(command.qrCodeBase64())),
                    "image/png"
            );
            mailSender.send(message);
        } catch (Exception exception) {
            throw new IllegalStateException("OTP 등록 메일 발송에 실패했습니다.", exception);
        }
    }

    /**
     * 서비스 이름을 포함한 OTP QR 등록 안내 메일 제목을 만든다.
     */
    private String createSubject() {
        return otpProperties.serviceName() + " 관리자 OTP QR 등록";
    }

    /**
     * 수신자명과 인라인 QR 이미지를 포함한 OTP 등록 안내 HTML을 만든다.
     */
    private String createHtmlBody(String recipientName) {
        String serviceDisplayName = HtmlUtils.htmlEscape(otpProperties.serviceName()) + " Admin";
        String escapedRecipientName = StringUtils.hasText(recipientName)
                ? HtmlUtils.htmlEscape(recipientName) + "님,"
                : "관리자님,";
        int qrSize = otpProperties.qrSize();

        return """
                <!DOCTYPE html>
                <html lang="ko">
                <body style="margin:0;padding:0;background-color:#f3f6fb;">
                <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="width:100%%;margin:0;padding:24px 0;background-color:#f3f6fb;">
                  <tr>
                    <td align="center" style="padding:0 16px;">
                      <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="600" style="width:100%%;max-width:600px;background-color:#ffffff;border:1px solid #e5e7eb;border-radius:24px;overflow:hidden;">
                        <tr>
                          <td style="padding:36px 40px;background-color:#0f3d75;color:#ffffff;">
                            <div style="font-size:12px;letter-spacing:1.6px;text-transform:uppercase;color:#c7d7ee;">%s</div>
                            <h1 style="margin:12px 0 0;font-size:28px;line-height:38px;font-weight:700;color:#ffffff;">OTP QR 등록 안내</h1>
                            <p style="margin:16px 0 0;font-size:15px;line-height:24px;color:#eaf1fb;">
                              안녕하세요 %s<br>
                              아래 QR 코드를 스캔해 관리자 계정의 2단계 인증을 등록해 주세요.
                            </p>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:32px 40px 16px;">
                            <div style="padding:24px;background-color:#f8fafc;border:1px solid #e5e7eb;border-radius:18px;">
                              <div style="margin:0 0 16px;font-size:16px;font-weight:700;color:#111827;">등록 방법</div>
                              <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="width:100%%;">
                                <tr>
                                  <td valign="top" style="width:32px;padding:0 12px 12px 0;">
                                    <div style="width:28px;height:28px;line-height:28px;text-align:center;background-color:#0f3d75;border-radius:50%%;font-size:14px;font-weight:700;color:#ffffff;">1</div>
                                  </td>
                                  <td style="padding:2px 0 12px;font-size:14px;line-height:22px;color:#374151;">Google Authenticator 또는 Microsoft Authenticator 앱을 설치해 주세요.</td>
                                </tr>
                                <tr>
                                  <td valign="top" style="width:32px;padding:0 12px 0 0;">
                                    <div style="width:28px;height:28px;line-height:28px;text-align:center;background-color:#0f3d75;border-radius:50%%;font-size:14px;font-weight:700;color:#ffffff;">2</div>
                                  </td>
                                  <td style="padding:2px 0 0;font-size:14px;line-height:22px;color:#374151;">앱에서 QR 코드 스캔 또는 계정 추가를 선택한 뒤 아래 코드를 스캔해 주세요.</td>
                                </tr>
                              </table>
                            </div>
                          </td>
                        </tr>
                        <tr>
                          <td align="center" style="padding:8px 40px 16px;">
                            <div style="display:inline-block;padding:18px;background-color:#ffffff;border:1px solid #dbe5f1;border-radius:20px;">
                              <img src="cid:%s" alt="OTP QR Code" width="%d" height="%d" style="display:block;width:%dpx;height:%dpx;border:0;outline:none;text-decoration:none;">
                            </div>
                            <p style="margin:20px 0 0;font-size:14px;line-height:22px;color:#4b5563;">QR 코드 인식이 어려우면 화면 밝기를 높이거나 메일을 PC 화면에서 열어 다시 스캔해 주세요.</p>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:0 40px 20px;">
                            <div style="padding:16px 18px;background-color:#eff6ff;border:1px solid #bfdbfe;border-radius:14px;">
                              <div style="margin:0 0 6px;font-size:13px;font-weight:700;color:#1d4ed8;">보안 안내</div>
                              <p style="margin:0;font-size:13px;line-height:21px;color:#334155;">QR 등록이 완료되면 앱에 표시되는 6자리 OTP 번호를 로그인 화면에 입력해 인증을 마무리해 주세요. 이 QR 코드는 관리자 계정의 인증 정보이므로 타인과 공유하지 마세요.</p>
                            </div>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:0 40px 32px;font-size:12px;line-height:20px;color:#94a3b8;">
                            본 메일은 발신 전용입니다.
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>
                </table>
                </body>
                </html>
                """.formatted(
                serviceDisplayName,
                escapedRecipientName,
                "otpQr",
                qrSize,
                qrSize,
                qrSize,
                qrSize
        );
    }

}
