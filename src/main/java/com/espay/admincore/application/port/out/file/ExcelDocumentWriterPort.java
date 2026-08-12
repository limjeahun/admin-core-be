package com.espay.admincore.application.port.out.file;

import com.espay.admincore.common.excel.ExcelDocument;

/**
 * 애플리케이션 서비스가 특정 스프레드시트 라이브러리에 의존하지 않고 표 데이터를 문서로 만드는 출력 포트.
 */
@FunctionalInterface
public interface ExcelDocumentWriterPort {

    /**
     * 전달받은 기술 중립 문서 모델을 XLSX로 직렬화한다.
     *
     * @param document 기술 중립적인 Excel 문서 모델
     * @return 생성된 XLSX 문서 바이트
     */
    byte[] write(ExcelDocument document);
}
