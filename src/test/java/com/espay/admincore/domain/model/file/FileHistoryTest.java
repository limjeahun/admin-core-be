package com.espay.admincore.domain.model.file;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileHistoryTest {

    @Test
    void downloadSucceededConvertsBytesToRoundedKilobytes() {
        FileHistory history = FileHistory.downloadSucceeded(
                "1", "USERS", "users.xlsx", 1536, "127.0.0.1");

        assertThat(history.getFileSize()).isEqualTo(2L);
        assertThat(history.isSuccess()).isTrue();
        assertThat(history.getFailReason()).isNull();
    }

    @Test
    void downloadSucceededRecordsAtLeastOneKilobyte() {
        FileHistory history = FileHistory.downloadSucceeded(
                "1", "USERS", "users.xlsx", 0, "127.0.0.1");

        assertThat(history.getFileSize()).isEqualTo(1L);
    }

    @Test
    void downloadSucceededRejectsNegativeFileSize() {
        assertThatThrownBy(() -> FileHistory.downloadSucceeded(
                "1", "USERS", "users.xlsx", -1, "127.0.0.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("파일 크기는 음수일 수 없습니다.");
    }

    @Test
    void downloadFailedRecordsUnknownFileSize() {
        FileHistory history = FileHistory.downloadFailed(
                "1", "USERS", "users.xlsx", "파일 생성 실패", "127.0.0.1");

        assertThat(history.getFileSize()).isNull();
        assertThat(history.isSuccess()).isFalse();
        assertThat(history.getFailReason()).isEqualTo("파일 생성 실패");
    }
}
