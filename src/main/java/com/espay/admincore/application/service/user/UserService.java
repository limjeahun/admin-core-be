package com.espay.admincore.application.service.user;

import com.espay.admincore.application.dto.user.*;
import com.espay.admincore.application.port.in.user.UserCommandUseCase;
import com.espay.admincore.application.port.in.user.UserQueryUseCase;
import com.espay.admincore.application.port.out.auth.PasswordHashPort;
import com.espay.admincore.application.port.out.role.RolePersistencePort;
import com.espay.admincore.application.port.out.user.UserLookupPort;
import com.espay.admincore.application.port.out.user.UserPersistencePort;
import com.espay.admincore.application.port.out.user.UserSearchPort;
import com.espay.admincore.application.exception.BusinessException;
import com.espay.admincore.application.exception.ErrorCode;
import com.espay.admincore.domain.exception.RoleNotFoundException;
import com.espay.admincore.domain.exception.UserNotFoundException;
import com.espay.admincore.domain.model.role.AdminRole;
import com.espay.admincore.domain.model.user.AdminUser;
import com.espay.admincore.domain.model.user.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 관리자 사용자 생성·수정·비밀번호 초기화와 상세·목록 조회를 처리하는 서비스.
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserCommandUseCase, UserQueryUseCase {
    private final UserLookupPort userLookupPort;
    private final UserPersistencePort userPersistencePort;
    private final UserSearchPort userSearchPort;
    private final RolePersistencePort rolePersistencePort;
    private final PasswordHashPort passwordHashPort;

    /**
     * 로그인 ID와 이메일 중복, 활성 권한을 검증하고 비밀번호를 BCrypt로 암호화해 저장한다.
     *
     * @param command 신규 사용자 정보와 원문 비밀번호
     * @return 생성된 사용자와 권한 정보
     */
    @Override
    @Transactional
    public UserResult createUser(CreateUserCommand command) {
        if (userLookupPort.existsByLoginId(command.loginId())) {
            throw new BusinessException(ErrorCode.USER_DUPLICATE, "이미 사용 중인 아이디입니다.");
        }
        ensureEmailAvailable(command.email(), null);
        AdminRole role = activeRole(command.roleId());
        AdminUser user = AdminUser.create(
                command.loginId().trim(), command.name().trim(), command.email().trim(), command.phoneNo(),
                command.deptName(), role.getId(), passwordHashPort.encode(command.password()), status(command.status()));
        return UserResult.from(userPersistencePort.save(user), role);
    }

    /**
     * 생략된 필드는 유지하면서 사용자 프로필, 권한과 상태를 변경한다.
     *
     * @param command 대상 사용자 ID와 변경 정보
     * @return 수정된 사용자와 권한 정보
     */
    @Override
    @Transactional
    public UserResult updateUser(UpdateUserCommand command) {
        AdminUser user = find(command.userId());
        String roleId = command.roleId() == null || command.roleId().isBlank() ? user.getRoleId() : command.roleId();
        AdminRole role = activeRole(roleId);
        String email = command.email() == null || command.email().isBlank() ? user.getEmail() : command.email().trim();
        ensureEmailAvailable(email, user.getId());
        AdminUser updated = user.update(
                command.name() == null || command.name().isBlank() ? user.getName() : command.name().trim(),
                email,
                command.phoneNo() == null ? user.getPhoneNo() : command.phoneNo(),
                command.deptName() == null ? user.getDeptName() : command.deptName(),
                role.getId(),
                command.status() == null || command.status().isBlank() ? user.getStatus() : status(command.status())
        );
        return UserResult.from(userPersistencePort.save(updated), role);
    }

    /**
     * 확인값과 기존 비밀번호 중복을 검사하고 새 비밀번호를 BCrypt로 암호화해 저장한다.
     *
     * @param command 대상 사용자와 새 비밀번호·확인값
     * @return 비밀번호가 변경된 사용자 정보
     */
    @Override
    @Transactional
    public UserResult changePassword(ChangeUserPasswordCommand command) {
        if (!command.newPassword().equals(command.confirmPassword())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }
        AdminUser user = find(command.userId());
        if (passwordHashPort.matches(command.newPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이전 비밀번호와 다르게 설정해 주세요.");
        }
        AdminRole role = rolePersistencePort.findById(user.getRoleId()).orElse(null);
        return UserResult.from(
                userPersistencePort.save(user.changePassword(passwordHashPort.encode(command.newPassword()))), role);
    }

    /**
     * 사용자 ID로 사용자와 부여된 권한 정보를 조회한다.
     *
     * @param userId 조회할 사용자 ID
     * @return 사용자 상세 결과
     */
    @Override
    @Transactional(readOnly = true)
    public UserResult getUser(String userId) {
        AdminUser user = find(userId);
        return UserResult.from(user, rolePersistencePort.findById(user.getRoleId()).orElse(null));
    }

    /**
     * 조건에 맞는 사용자 페이지와 필요한 권한명을 일괄 조합한다.
     *
     * @param query 역할, 상태, 검색어와 페이지 조건
     * @return 사용자 페이지 결과
     */
    @Override
    @Transactional(readOnly = true)
    public UserListResult getUsers(UserQuery query) {
        var users = userSearchPort.findPage(query);
        Map<String, AdminRole> roles = users.stream().map(AdminUser::getRoleId).distinct()
                .map(rolePersistencePort::findById).flatMap(java.util.Optional::stream)
                .collect(Collectors.toMap(AdminRole::getId, role -> role));
        var items = users.stream().map(user -> UserResult.from(user, roles.get(user.getRoleId()))).toList();
        return new UserListResult(items, userSearchPort.count(query), query.page(), query.size());
    }

    /**
     * 사용자 ID로 사용자를 찾고 없으면 도메인 예외를 발생시킨다.
     *
     * @param userId 사용자 ID
     * @return 조회된 사용자
     */
    private AdminUser find(String userId) {
        return userLookupPort.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    /**
     * 사용자에게 부여할 권한이 존재하고 활성 상태인지 확인한다.
     *
     * @param roleId 권한 ID
     * @return 활성 권한
     */
    private AdminRole activeRole(String roleId) {
        AdminRole role = rolePersistencePort.findById(roleId).orElseThrow(() -> new RoleNotFoundException(roleId));
        if (!role.isActive()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "사용 중인 권한만 부여할 수 있습니다.");
        }
        return role;
    }

    /**
     * 외부 상태 표현을 도메인 상태로 변환하고 오류를 API 입력 오류로 바꾼다.
     *
     * @param value 사용자 상태 표현값
     * @return 변환된 사용자 상태
     */
    private UserStatus status(String value) {
        try {
            return UserStatus.from(value);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, exception.getMessage());
        }
    }

    /**
     * 다른 사용자가 동일한 이메일을 사용 중인지 확인한다.
     *
     * @param email 저장할 이메일
     * @param currentUserId 수정 중인 사용자 ID, 신규 생성이면 {@code null}
     */
    private void ensureEmailAvailable(String email, String currentUserId) {
        userLookupPort.findByEmail(email).filter(user -> currentUserId == null
                        || !currentUserId.equals(user.getId()))
                .ifPresent(user -> { throw new BusinessException(ErrorCode.USER_DUPLICATE, "이미 사용 중인 이메일입니다."); });
    }
}
