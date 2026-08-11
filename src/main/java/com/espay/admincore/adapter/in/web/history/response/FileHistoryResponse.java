package com.espay.admincore.adapter.in.web.history.response;

import com.espay.admincore.application.dto.history.FileHistoryResult;

import java.time.LocalDateTime;

/**
 * 단일 파일 업로드·다운로드 감사 이력 HTTP 응답.
 *
 * @param historyId 이력 ID
 * @param userId 작업 사용자 ID
 * @param userName 작업 사용자명
 * @param loginId 작업 로그인 ID
 * @param ioType 업로드(U) 또는 다운로드(D)
 * @param menuCode 작업 메뉴 코드
 * @param menuName 작업 메뉴명
 * @param fileName 파일명
 * @param fileSize 파일 크기(KB)
 * @param success 성공 여부
 * @param failReason 실패 사유
 * @param clientIp 요청 IP
 * @param createdAt 작업 시각
 */
public record FileHistoryResponse(String historyId, String userId, String userName, String loginId,
                                  String ioType, String menuCode, String menuName, String fileName,
                                  Long fileSize, boolean success, String failReason, String clientIp,
                                  LocalDateTime createdAt) {
    /**
     * 애플리케이션 파일 이력 결과를 HTTP 응답으로 변환한다.
     *
     * @param result 변환할 파일 이력 결과
     * @return 외부 응답용 파일 이력
     */
    public static FileHistoryResponse from(FileHistoryResult result) {
        return new FileHistoryResponse(result.historyId(), result.userId(), result.userName(), result.loginId(),
                result.ioType(), result.menuCode(), result.menuName(), result.fileName(), result.fileSize(),
                result.success(), result.failReason(), result.clientIp(), result.createdAt());
    }
}
