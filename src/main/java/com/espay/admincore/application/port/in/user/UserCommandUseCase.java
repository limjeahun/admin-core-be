package com.espay.admincore.application.port.in.user;

import com.espay.admincore.application.dto.user.ChangeUserPasswordCommand;
import com.espay.admincore.application.dto.user.CreateUserCommand;
import com.espay.admincore.application.dto.user.UpdateUserCommand;
import com.espay.admincore.application.dto.user.UserResult;

/**
 * 관리자 사용자 생성, 정보 변경과 비밀번호 초기화를 처리하는 명령 유스케이스.
 */
public interface UserCommandUseCase {
    /**
     * 로그인 ID와 이메일 중복, 권한 상태를 검증하고 신규 사용자를 생성한다.
     *
     * @param command 신규 사용자와 원문 비밀번호 정보
     * @return 생성된 사용자와 권한 정보
     */
    UserResult createUser(CreateUserCommand command);
    /**
     * 기존 사용자의 프로필, 권한과 활성 상태를 수정한다.
     *
     * @param command 대상 사용자 ID와 변경 정보
     * @return 수정된 사용자와 권한 정보
     */
    UserResult updateUser(UpdateUserCommand command);
    /**
     * 새 비밀번호 확인과 기존 비밀번호 중복을 검사한 뒤 비밀번호를 재설정한다.
     *
     * @param command 대상 사용자와 새 비밀번호 정보
     * @return 비밀번호가 변경된 사용자 정보
     */
    UserResult changePassword(ChangeUserPasswordCommand command);
}
