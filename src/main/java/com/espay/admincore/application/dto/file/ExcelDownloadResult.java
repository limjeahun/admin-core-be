package com.espay.admincore.application.dto.file;

/**
 * 생성이 완료된 Excel 문서의 다운로드 결과.
 *
 * @param fileName Content-Disposition 헤더에 사용할 파일명
 * @param fileBytes 클라이언트에 전송할 Excel 바이너리
 */
public record ExcelDownloadResult(String fileName, byte[] fileBytes) {
    /**
     * Excel 문서의 다운로드 결과를 반환한다.
     * @param fileName 파일명
     * @param fileBytes Excel 파일 바이너리
     * @return ExcelDownloadResult 인스턴스
     */
    public static ExcelDownloadResult of(String fileName, byte[] fileBytes) {
        return new ExcelDownloadResult(fileName, fileBytes);
    }
}
