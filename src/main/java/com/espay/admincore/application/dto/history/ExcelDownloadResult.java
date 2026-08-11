package com.espay.admincore.application.dto.history;

/**
 * 생성이 완료된 Excel 문서의 다운로드 결과.
 *
 * @param fileName Content-Disposition 헤더에 사용할 파일명
 * @param fileBytes 클라이언트에 전송할 Excel 바이너리
 */
public record ExcelDownloadResult(String fileName, byte[] fileBytes) {
}
