package com.espay.admincore.application.exception;

/** Excel 문서를 파일로 생성하지 못했을 때 발생하는 애플리케이션 예외. */
public final class ExcelDocumentWriteException extends RuntimeException {

    /**
     * 원본 문서 생성 실패 원인을 포함해 예외를 생성한다.
     *
     * @param cause 문서 생성 실패 원인
     */
    public ExcelDocumentWriteException(Throwable cause) {
        super("Excel 문서를 생성하지 못했습니다.", cause);
    }
}
