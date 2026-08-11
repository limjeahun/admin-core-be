package com.espay.admincore.domain.model.file;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 관리 기능에서 발생한 파일 업로드·다운로드 감사 이력 Aggregate Root.
 */
@Getter
public final class FileHistory {
    /** 바이트 크기를 KB로 환산할 때 사용하는 1KB의 바이트 수다. */
    private static final double BYTES_PER_KILOBYTE = 1024.0;

    /** 영속화 이후 부여되는 파일 이력 식별자이며 신규 이력에서는 {@code null}이다. */
    private final String id;
    /** 파일 작업을 수행한 관리자 사용자 ID다. */
    private final String userId;
    /** 조회 시 사용자 테이블에서 조합하는 표시용 사용자명이며 저장 시에는 {@code null}이다. */
    private final String userName;
    /** 조회 시 사용자 테이블에서 조합하는 로그인 ID이며 저장 시에는 {@code null}이다. */
    private final String loginId;
    /** 업로드 또는 다운로드 구분을 나타내는 DB 저장값 {@code U}/{@code D}다. */
    private final String ioType;
    /** 파일 작업이 발생한 관리자 메뉴의 고유 코드다. */
    private final String menuCode;
    /** 조회 시 메뉴 카탈로그에서 조합하는 표시용 메뉴명이며 저장 시에는 {@code null}이다. */
    private final String menuName;
    /** 업로드하거나 다운로드한 파일명이다. */
    private final String fileName;
    /** 파일 크기를 KB 단위로 반올림한 값이며 크기를 확인할 수 없으면 {@code null}이다. */
    private final Long fileSize;
    /** 파일 작업이 성공적으로 완료됐는지 나타내는 결과다. */
    private final boolean success;
    /** 파일 작업 실패 사유이며 성공한 이력에서는 {@code null}이다. */
    private final String failReason;
    /** 파일 작업을 요청한 클라이언트 IP 주소다. */
    private final String clientIp;
    /** 파일 작업이 발생한 시각이다. */
    private final LocalDateTime createdAt;

    /**
     * 신규 생성과 영속·조회 상태 복원에 공통으로 사용하는 내부 생성자다.
     *
     * @param id 영속화된 이력 ID, 신규 이력이면 {@code null}
     * @param userId 파일 작업을 수행한 사용자 ID
     * @param userName 조회 시 조합한 사용자명
     * @param loginId 조회 시 조합한 로그인 ID
     * @param ioType 업로드 또는 다운로드 구분값
     * @param menuCode 작업이 발생한 메뉴 코드
     * @param menuName 조회 시 조합한 메뉴명
     * @param fileName 처리한 파일명
     * @param fileSize 파일 크기(KB), 확인할 수 없으면 {@code null}
     * @param success 파일 작업 성공 여부
     * @param failReason 실패 사유, 성공한 경우 {@code null}
     * @param clientIp 요청 클라이언트 IP
     * @param createdAt 파일 작업 발생 시각
     */
    private FileHistory(String id, String userId, String userName, String loginId, String ioType, String menuCode,
                        String menuName, String fileName, Long fileSize, boolean success, String failReason,
                        String clientIp, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.loginId = loginId;
        this.ioType = ioType;
        this.menuCode = menuCode;
        this.menuName = menuName;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.success = success;
        this.failReason = failReason;
        this.clientIp = clientIp;
        this.createdAt = createdAt;
    }

    /**
     * 파일 다운로드에 성공한 신규 감사 이력을 생성한다.
     *
     * @param userId 다운로드를 수행한 사용자 ID
     * @param menuCode 다운로드가 발생한 메뉴 코드
     * @param fileName 다운로드한 파일명
     * @param fileSizeInBytes 생성된 파일 크기(byte)
     * @param clientIp 요청 클라이언트 IP
     * @return 다운로드 성공 상태와 현재 시각을 가진 신규 이력
     * @throws IllegalArgumentException 파일 크기가 음수인 경우
     */
    public static FileHistory downloadSucceeded(String userId, String menuCode, String fileName,
                                                long fileSizeInBytes, String clientIp) {
        return create(userId, "D", menuCode, fileName, toRoundedKilobytes(fileSizeInBytes),
                true, null, clientIp);
    }

    /**
     * 파일 다운로드에 실패한 신규 감사 이력을 생성한다.
     *
     * @param userId 다운로드를 시도한 사용자 ID
     * @param menuCode 다운로드가 발생한 메뉴 코드
     * @param fileName 다운로드하려던 파일명
     * @param failReason 다운로드 실패 사유
     * @param clientIp 요청 클라이언트 IP
     * @return 다운로드 실패 상태와 현재 시각을 가진 신규 이력
     */
    public static FileHistory downloadFailed(String userId, String menuCode, String fileName,
                                             String failReason, String clientIp) {
        return create(userId, "D", menuCode, fileName, null, false, failReason, clientIp);
    }

    /**
     * 바이트 단위 파일 크기를 감사 이력의 저장 단위인 KB로 반올림한다.
     *
     * <p>생성된 파일은 크기가 0byte여도 최소 1KB로 기록한다.</p>
     *
     * @param fileSizeInBytes 생성된 파일 크기(byte)
     * @return 반올림한 KB 크기, 최소 {@code 1}
     * @throws IllegalArgumentException 파일 크기가 음수인 경우
     */
    private static long toRoundedKilobytes(long fileSizeInBytes) {
        if (fileSizeInBytes < 0) {
            throw new IllegalArgumentException("파일 크기는 음수일 수 없습니다.");
        }
        return Math.max(1L, Math.round(fileSizeInBytes / BYTES_PER_KILOBYTE));
    }

    /**
     * 파일 작업 구분과 결과가 확정된 신규 이력을 생성하는 내부 팩토리다.
     *
     * @param userId 파일 작업을 수행한 사용자 ID
     * @param ioType 업로드 또는 다운로드 구분값
     * @param menuCode 작업이 발생한 메뉴 코드
     * @param fileName 처리한 파일명
     * @param fileSize 파일 크기(KB)
     * @param success 파일 작업 성공 여부
     * @param failReason 실패 사유
     * @param clientIp 요청 클라이언트 IP
     * @return 아직 ID가 없는 현재 시각의 파일 이력
     */
    private static FileHistory create(String userId, String ioType, String menuCode, String fileName,
                                      Long fileSize, boolean success, String failReason, String clientIp) {
        return new FileHistory(null, userId, null, null, normalizeIoType(ioType), menuCode, null, fileName,
                fileSize, success, failReason, clientIp, LocalDateTime.now());
    }

    /**
     * 영속성 또는 조회 결과의 기존 파일 이력을 Aggregate로 복원한다.
     *
     * @param id 영속화된 이력 ID
     * @param userId 파일 작업을 수행한 사용자 ID
     * @param userName 조회 시 사용자 테이블에서 조합한 사용자명
     * @param loginId 조회 시 사용자 테이블에서 조합한 로그인 ID
     * @param ioType 업로드 또는 다운로드 구분값
     * @param menuCode 작업이 발생한 메뉴 코드
     * @param menuName 조회 시 메뉴 카탈로그에서 조합한 메뉴명
     * @param fileName 처리한 파일명
     * @param fileSize 파일 크기(KB), 확인할 수 없으면 {@code null}
     * @param success 파일 작업 성공 여부
     * @param failReason 실패 사유, 성공한 경우 {@code null}
     * @param clientIp 요청 클라이언트 IP
     * @param createdAt 파일 작업 발생 시각
     * @return 영속 또는 조회 상태로 복원된 파일 이력 Aggregate
     */
    public static FileHistory reconstitute(String id, String userId, String userName, String loginId,
                                           String ioType, String menuCode, String menuName, String fileName,
                                           Long fileSize, boolean success, String failReason, String clientIp,
                                           LocalDateTime createdAt) {
        return new FileHistory(id, userId, userName, loginId, ioType, menuCode, menuName, fileName,
                fileSize, success, failReason, clientIp, createdAt);
    }

    /**
     * 외부의 업로드·다운로드 표현을 DB 저장 형식으로 정규화한다.
     *
     * @param ioType U, UPLOAD, D 또는 DOWNLOAD 값
     * @return 정규화된 {@code U} 또는 {@code D}
     * @throws IllegalArgumentException 값이 없거나 지원하지 않는 구분인 경우
     */
    private static String normalizeIoType(String ioType) {
        if (ioType == null) {
            throw new IllegalArgumentException("파일 처리 구분은 필수입니다.");
        }
        return switch (ioType.trim().toUpperCase()) {
            case "U", "UPLOAD" -> "U";
            case "D", "DOWNLOAD" -> "D";
            default -> throw new IllegalArgumentException("지원하지 않는 파일 처리 구분입니다: " + ioType);
        };
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof FileHistory other && id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
