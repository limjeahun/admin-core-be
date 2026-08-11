package com.espay.admincore.application.dto.history;

import com.espay.admincore.domain.model.file.FileHistory;

import java.time.LocalDateTime;

/**
 * 화면과 Excel에 표시할 파일 업로드·다운로드 감사 이력.
 *
 * @param historyId 파일 이력 ID
 * @param userId 작업을 수행한 사용자 ID
 * @param userName 작업을 수행한 사용자명
 * @param loginId 작업을 수행한 로그인 ID
 * @param ioType 업로드(U) 또는 다운로드(D) 구분
 * @param menuCode 작업이 발생한 메뉴 코드
 * @param menuName 작업이 발생한 메뉴명
 * @param fileName 처리한 파일명
 * @param fileSize 파일 크기(KB)
 * @param success 처리 성공 여부
 * @param failReason 실패한 경우의 사유
 * @param clientIp 요청 클라이언트 IP
 * @param createdAt 이력 생성 시각
 */
public record FileHistoryResult(String historyId, String userId, String userName, String loginId,
                                String ioType, String menuCode, String menuName, String fileName,
                                Long fileSize, boolean success, String failReason, String clientIp,
                                LocalDateTime createdAt) {
    /**
     * 파일 이력 도메인 모델을 애플리케이션 조회 결과로 변환한다.
     *
     * @param history 변환할 파일 이력 도메인 모델
     * @return 모든 조회 표시 필드를 복사한 결과
     */
    public static FileHistoryResult from(FileHistory history) {
        return new FileHistoryResult(history.getId(), history.getUserId(), history.getUserName(), history.getLoginId(),
                history.getIoType(), history.getMenuCode(), history.getMenuName(), history.getFileName(), history.getFileSize(),
                history.isSuccess(), history.getFailReason(), history.getClientIp(), history.getCreatedAt());
    }
}
