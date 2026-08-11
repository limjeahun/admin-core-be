package com.espay.admincore.application.dto.user;

/**
 * 관리자가 지정한 사용자의 비밀번호를 초기화하는 명령.
 *
 * @param userId 비밀번호를 변경할 사용자 ID
 * @param newPassword 새로 저장할 원문 비밀번호
 * @param confirmPassword 입력 오류를 방지하기 위한 비밀번호 확인값
 */
public record ChangeUserPasswordCommand(String userId, String newPassword, String confirmPassword) {
    /**
     * 사용자와 새 비밀번호·확인값으로 변경 명령을 생성한다.
     * @param userId 비밀번호를 변경할 사용자 ID
     * @param newPassword 새로 저장할 원문 비밀번호
     * @param confirmPassword 비밀번호 확인값
     * @return 사용자 비밀번호 변경 명령
     */
    public static ChangeUserPasswordCommand of(String userId, String newPassword, String confirmPassword) {
        return new ChangeUserPasswordCommand(userId, newPassword, confirmPassword);
    }
}
