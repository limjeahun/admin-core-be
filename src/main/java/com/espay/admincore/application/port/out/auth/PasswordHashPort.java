package com.espay.admincore.application.port.out.auth;

/**
 * 평문 비밀번호의 단방향 해시 생성과 일치 검증을 외부 암호화 구현에 위임하는 출력 포트.
 */
public interface PasswordHashPort {

    /**
     * 저장 가능한 단방향 비밀번호 해시를 생성한다.
     *
     * @param rawPassword 해시할 평문 비밀번호
     * @return 솔트와 알고리즘 정보가 포함된 비밀번호 해시
     */
    String encode(String rawPassword);

    /**
     * 평문 비밀번호가 저장된 해시와 같은 비밀번호인지 검증한다.
     *
     * @param rawPassword 사용자가 입력한 평문 비밀번호
     * @param encodedPassword 저장된 단방향 비밀번호 해시
     * @return 같은 비밀번호이면 {@code true}
     */
    boolean matches(String rawPassword, String encodedPassword);
}
