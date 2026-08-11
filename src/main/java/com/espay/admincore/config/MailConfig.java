package com.espay.admincore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * OTP 안내 메일을 전송할 SMTP 클라이언트를 구성한다.
 */
@Configuration
public class MailConfig {

    /**
     * SMTP 서버 접속 정보와 전송 프로퍼티를 설정한 메일 발송 빈을 생성한다.
     *
     * @param host SMTP 서버 주소
     * @param port SMTP 서버 포트
     * @param username SMTP 로그인 계정이자 메일 발신 주소
     * @param password SMTP 로그인 비밀번호
     * @return OTP 안내 메일 발송에 사용할 SMTP 클라이언트
     */
    @Bean
    public JavaMailSenderImpl javaMailSender(
            @Value("${spring.mail.host}") String host,
            @Value("${spring.mail.port}") int port,
            @Value("${spring.mail.username}") String username,
            @Value("${spring.mail.password}") String password
    ) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties properties = mailSender.getJavaMailProperties();
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.ssl.checkserveridentity", "false");
        properties.put("mail.debug", "true");
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");

        return mailSender;
    }
}
