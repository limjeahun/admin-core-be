package com.espay.admincore.adapter.out.security.otp;

import com.espay.admincore.application.dto.auth.GenerateOtpQrCodeCommand;
import com.espay.admincore.application.port.out.auth.OtpQrCodeGeneratorPort;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

/**
 * ZXing으로 OTP 등록 URI를 PNG QR 이미지로 생성하는 어댑터.
 */
@Component
public class OtpQrCodeGeneratorAdapter implements OtpQrCodeGeneratorPort {
    /**
     * 콘텐츠를 지정 크기 QR 매트릭스로 인코딩하고 PNG 바이트를 Base64 문자열로 반환한다.
     *
     * @param command OTP URI와 이미지 크기를 묶은 생성 명령
     * @return Base64 PNG 이미지
     * @throws IllegalStateException QR 인코딩 또는 이미지 쓰기에 실패한 경우
     */
    @Override
    public String generateBase64(GenerateOtpQrCodeCommand command) {
        try {
            var matrix = new QRCodeWriter().encode(
                    command.content(), BarcodeFormat.QR_CODE, command.width(), command.height());
            var output = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", output);
            return Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (Exception exception) {
            throw new IllegalStateException("OTP QR 코드 생성에 실패했습니다.", exception);
        }
    }
}
