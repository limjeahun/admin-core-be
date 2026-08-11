package com.espay.admincore.application.port.out.file;

import com.espay.admincore.application.dto.file.WriteExcelDocumentCommand;

/**
 * 애플리케이션 서비스가 특정 스프레드시트 라이브러리에 의존하지 않고 표 데이터를 문서로 만드는 출력 포트.
 */
public interface ExcelDocumentWriterPort {

    /**
     * 문서 종류에 맞는 양식을 선택하고 전달받은 요약과 본문 데이터를 XLSX로 직렬화한다.
     *
     * @param command 문서 종류, 조회 조건 요약과 본문 행을 묶은 생성 명령
     * @return 생성된 XLSX 문서 바이트
     */
    byte[] write(WriteExcelDocumentCommand command);
}
