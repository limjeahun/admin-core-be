CREATE TABLE IF NOT EXISTS roles (
    role_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '권한 고유번호',
    role_name VARCHAR(100) NOT NULL COMMENT '권한명',
    role_desc VARCHAR(255) NULL COMMENT '권한 설명',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부(Y/N)',
    created_at DATETIME(6) NOT NULL COMMENT '생성일시',
    updated_at DATETIME(6) NOT NULL COMMENT '수정일시',
    PRIMARY KEY (role_id),
    UNIQUE KEY uq_roles_name (role_name),
    KEY idx_roles_use_yn (use_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='관리자 권한';

CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '사용자 고유번호',
    login_id VARCHAR(100) NOT NULL COMMENT '로그인 아이디',
    user_name VARCHAR(100) NOT NULL COMMENT '사용자명',
    email VARCHAR(100) NOT NULL COMMENT '이메일',
    phone_no VARCHAR(20) NULL COMMENT '휴대폰번호',
    dept_name VARCHAR(50) NULL COMMENT '소속/부서',
    role_id BIGINT NOT NULL COMMENT '권한 ID',
    password_hash VARCHAR(255) NOT NULL COMMENT '비밀번호 해시',
    user_status CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 상태(Y/N)',
    otp_secret VARCHAR(100) NULL COMMENT 'OTP Secret',
    last_login_at DATETIME(6) NULL COMMENT '최종 로그인 일시',
    created_at DATETIME(6) NOT NULL COMMENT '생성일시',
    updated_at DATETIME(6) NOT NULL COMMENT '수정일시',
    PRIMARY KEY (user_id),
    UNIQUE KEY uq_users_login_id (login_id),
    UNIQUE KEY uq_users_email (email),
    KEY idx_users_role_id (role_id),
    KEY idx_users_name (user_name),
    KEY idx_users_status (user_status),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='관리자 사용자';

CREATE TABLE IF NOT EXISTS permissions (
    role_id BIGINT NOT NULL COMMENT '권한 ID',
    menu_code VARCHAR(30) NOT NULL COMMENT '메뉴 코드',
    can_view CHAR(1) NOT NULL DEFAULT 'N' COMMENT '조회 권한(Y/N)',
    can_edit CHAR(1) NOT NULL DEFAULT 'N' COMMENT '편집 권한(Y/N)',
    created_at DATETIME(6) NOT NULL COMMENT '생성일시',
    updated_at DATETIME(6) NOT NULL COMMENT '수정일시',
    PRIMARY KEY (role_id, menu_code),
    KEY idx_permissions_menu_code (menu_code),
    CONSTRAINT fk_permissions_role FOREIGN KEY (role_id) REFERENCES roles(role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='권한별 메뉴 접근 권한';

CREATE TABLE IF NOT EXISTS login_logs (
    hist_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '로그인 이력 고유번호',
    user_id BIGINT NULL COMMENT '식별된 사용자 ID',
    auth_step VARCHAR(20) NOT NULL COMMENT '인증 단계(LOGIN/OTP)',
    result_yn CHAR(1) NOT NULL COMMENT '성공 여부(Y/N)',
    login_reason VARCHAR(100) NULL COMMENT '접속 사유',
    fail_reason VARCHAR(255) NULL COMMENT '실패 사유',
    input_id VARCHAR(100) NULL COMMENT '입력 로그인 아이디',
    client_ip VARCHAR(64) NULL COMMENT '접속 IP',
    user_agent VARCHAR(255) NULL COMMENT 'User-Agent',
    created_at DATETIME(6) NOT NULL COMMENT '시도 일시',
    PRIMARY KEY (hist_id),
    KEY idx_login_logs_user_id (user_id),
    KEY idx_login_logs_created_at (created_at),
    KEY idx_login_logs_step_result (auth_step, result_yn),
    CONSTRAINT fk_login_logs_user FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='로그인 및 OTP 인증 이력';

CREATE TABLE IF NOT EXISTS file_logs (
    hist_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '파일 이력 고유번호',
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    io_type CHAR(1) NOT NULL COMMENT '처리 구분(U/D)',
    menu_code VARCHAR(30) NOT NULL COMMENT '업무 메뉴 코드',
    file_name VARCHAR(255) NOT NULL COMMENT '파일명',
    file_size BIGINT NULL COMMENT '파일 크기(KB)',
    result_yn CHAR(1) NOT NULL COMMENT '성공 여부(Y/N)',
    fail_reason VARCHAR(255) NULL COMMENT '실패 사유',
    client_ip VARCHAR(64) NULL COMMENT '접속 IP',
    created_at DATETIME(6) NOT NULL COMMENT '처리 일시',
    PRIMARY KEY (hist_id),
    KEY idx_file_logs_user_id (user_id),
    KEY idx_file_logs_created_at (created_at),
    KEY idx_file_logs_menu_code (menu_code),
    CONSTRAINT fk_file_logs_user FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='파일 업로드 및 다운로드 이력';
