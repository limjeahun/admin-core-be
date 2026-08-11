package com.espay.admincore.adapter.in.web.user.request;

import com.espay.admincore.application.dto.user.ChangeUserPasswordCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 관리자 사용자의 비밀번호를 초기화하는 HTTP 요청.
 *
 * @param newPassword 영문·숫자·특수문자를 포함한 8~100자 새 비밀번호
 * @param confirmPassword 새 비밀번호와 일치해야 하는 확인값
 */
public record UserPasswordResetRequest(
        @NotBlank @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,100}$") String newPassword,
        @NotBlank String confirmPassword
) {
    /**
     * URL의 사용자 ID를 요청값과 결합해 비밀번호 변경 명령으로 변환한다.
     *
     * @param userId 비밀번호를 초기화할 사용자 ID
     * @return 비밀번호 변경 명령
     */
    public ChangeUserPasswordCommand toCommand(String userId) {
        return ChangeUserPasswordCommand.of(userId, newPassword, confirmPassword);
    }
}
