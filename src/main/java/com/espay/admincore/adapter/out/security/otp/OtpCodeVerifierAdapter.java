package com.espay.admincore.adapter.out.security.otp;

import com.espay.admincore.application.port.out.auth.OtpCodeVerifierPort;
import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * RFC 6238 방식의 6자리 HMAC-SHA1 TOTP를 직접 계산해 검증하는 어댑터.
 */
@Component
public class OtpCodeVerifierAdapter implements OtpCodeVerifierPort {
    private static final long TIME_STEP_SECONDS = 30L;
    private static final int WINDOW = 1;

    /**
     * 현재 30초 카운터와 앞뒤 한 구간에서 생성한 번호 중 입력값과 일치하는지 확인한다.
     *
     * @param encodedSecret Base32로 인코딩된 사용자 OTP 비밀키
     * @param code 사용자가 입력한 6자리 OTP 번호
     * @return 유효한 형식이고 허용 시간 창에서 일치하면 {@code true}
     */
    @Override
    public boolean verify(String encodedSecret, String code) {
        if (encodedSecret == null || code == null || !code.matches("\\d{6}")) {
            return false;
        }
        try {
            byte[] key = new Base32().decode(encodedSecret);
            long counter = System.currentTimeMillis() / 1000L / TIME_STEP_SECONDS;
            for (int offset = -WINDOW; offset <= WINDOW; offset++) {
                if (generate(key, counter + offset) == Integer.parseInt(code)) {
                    return true;
                }
            }
            return false;
        } catch (Exception exception) {
            return false;
        }
    }

    /**
     * 비밀키와 시간 카운터로 HMAC-SHA1 동적 절단 값을 계산해 6자리 번호를 만든다.
     *
     * @param key 디코딩된 OTP 비밀키
     * @param counter 30초 단위 시간 카운터
     * @return 0~999999 범위 TOTP 정수
     * @throws Exception MAC 알고리즘 또는 키 초기화에 실패한 경우
     */
    private int generate(byte[] key, long counter) throws Exception {
        byte[] counterBytes = new byte[8];
        for (int index = 7; index >= 0; index--) {
            counterBytes[index] = (byte) counter;
            counter >>>= 8;
        }
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(key, "HmacSHA1"));
        byte[] hash = mac.doFinal(counterBytes);
        int offset = hash[hash.length - 1] & 0x0f;
        int binary = ((hash[offset] & 0x7f) << 24)
                | ((hash[offset + 1] & 0xff) << 16)
                | ((hash[offset + 2] & 0xff) << 8)
                | (hash[offset + 3] & 0xff);
        return binary % 1_000_000;
    }

}
