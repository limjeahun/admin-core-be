package com.espay.admincore.adapter.in.web.user.request;

import com.espay.admincore.application.dto.user.CreateUserCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 신규 관리자 사용자를 등록하는 HTTP 요청.
 *
 * @param loginId 4~100자의 필수 로그인 ID
 * @param name 최대 100자의 필수 사용자명
 * @param email 형식 검증을 통과해야 하는 필수 이메일
 * @param phoneNo 연락처
 * @param deptName 소속 또는 부서명
 * @param roleId 부여할 필수 권한 ID
 * @param password 영문·숫자·특수문자를 포함한 8~100자 비밀번호
 * @param status 사용자 활성 상태 표현값
 */
public record UserCreateRequest(
        @NotBlank @Size(min = 4, max = 100) String loginId,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email String email,
        String phoneNo,
        String deptName,
        @NotBlank String roleId,
        @NotBlank @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,100}$",
                message = "비밀번호는 영문, 숫자, 특수문자를 포함해 8자 이상이어야 합니다.") String password,
        String status
) {
    /**
     * 검증을 마친 HTTP 요청을 사용자 생성 명령으로 변환한다.
     *
     * @return 사용자 생성 명령
     */
    public CreateUserCommand toCommand() {
        return CreateUserCommand.of(loginId, name, email, phoneNo, deptName, roleId, password, status);
    }
}
