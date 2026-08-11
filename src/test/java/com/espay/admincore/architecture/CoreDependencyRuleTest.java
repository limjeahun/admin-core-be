package com.espay.admincore.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 도메인과 애플리케이션 코어가 Spring 및 외부 어댑터 방향으로 의존하지 않는지 검사한다.
 */
class CoreDependencyRuleTest {

    private static final Path MAIN_SOURCE = Path.of("src/main/java/com/espay/admincore");

    /**
     * 애플리케이션 소스가 Jakarta 또는 adapter/config 패키지를 import하지 않는지 확인한다.
     *
     * @throws IOException Java 소스 목록 또는 내용을 읽지 못한 경우
     */
    @Test
    void applicationLayerDoesNotDependOnFrameworkOrAdapters() throws IOException {
        assertNoForbiddenImports(MAIN_SOURCE.resolve("application"), List.of(
                "import jakarta.",
                "import org.apache.poi.",
                "import com.espay.admincore.adapter.",
                "import com.espay.admincore.config."
        ));
    }

    /**
     * 애플리케이션 계층에서 서비스 등록과 트랜잭션 경계 외의 Spring API를 사용하지 않는지 확인한다.
     *
     * @throws IOException Java 소스 목록 또는 내용을 읽지 못한 경우
     */
    @Test
    void applicationLayerUsesOnlyServiceAndTransactionAnnotations() throws IOException {
        Path applicationRoot = MAIN_SOURCE.resolve("application");
        List<String> violations;
        try (var files = Files.walk(applicationRoot)) {
            violations = files
                    .filter(path -> path.toString().endsWith(".java"))
                    .flatMap(path -> lines(path).stream()
                            .map(String::trim)
                            .filter(line -> line.startsWith("import org.springframework."))
                            .filter(line -> !line.equals(
                                    "import org.springframework.transaction.annotation.Transactional;"))
                            .filter(line -> !line.equals(
                                    "import org.springframework.stereotype.Service;"))
                            .map(line -> path + " -> " + line))
                    .toList();
        }
        assertThat(violations).isEmpty();
    }

    /**
     * 도메인 소스가 애플리케이션·공통·어댑터·설정 또는 프레임워크 패키지에 의존하지 않는지 확인한다.
     *
     * @throws IOException Java 소스 목록 또는 내용을 읽지 못한 경우
     */
    @Test
    void domainLayerDependsOnlyOnJavaAndDomainTypes() throws IOException {
        assertNoForbiddenImports(MAIN_SOURCE.resolve("domain"), List.of(
                "import org.springframework.",
                "import org.apache.poi.",
                "import jakarta.",
                "import com.espay.admincore.application.",
                "import com.espay.admincore.adapter.",
                "import com.espay.admincore.common.",
                "import com.espay.admincore.config."
        ));
    }

    /**
     * 대상 소스 트리에서 금지된 import 문장을 수집하고 한 건도 없음을 검증한다.
     *
     * @param sourceRoot 검사할 계층의 Java 소스 루트
     * @param forbiddenImports 허용하지 않을 import 접두사 목록
     * @throws IOException Java 소스 탐색 또는 읽기에 실패한 경우
     */
    private void assertNoForbiddenImports(Path sourceRoot, List<String> forbiddenImports) throws IOException {
        try (var files = Files.walk(sourceRoot)) {
            List<String> violations = files
                    .filter(path -> path.toString().endsWith(".java"))
                    .flatMap(path -> forbiddenImports.stream()
                            .filter(prefix -> contains(path, prefix))
                            .map(prefix -> path + " -> " + prefix))
                    .toList();
            assertThat(violations).isEmpty();
        }
    }

    /**
     * Java 소스 파일에 지정한 import 접두사가 포함되어 있는지 확인한다.
     *
     * @param path 검사할 Java 소스 파일
     * @param value 찾을 import 접두사
     * @return 파일에 금지 import가 있으면 {@code true}
     */
    private boolean contains(Path path, String value) {
        try {
            return Files.readString(path).contains(value);
        } catch (IOException exception) {
            throw new IllegalStateException("아키텍처 검사 중 소스를 읽을 수 없습니다. path=" + path, exception);
        }
    }

    /**
     * Java 소스 파일을 줄 단위로 읽고 실패 시 검사 예외로 변환한다.
     *
     * @param path 읽을 Java 소스 파일
     * @return 소스의 전체 줄 목록
     */
    private List<String> lines(Path path) {
        try {
            return Files.readAllLines(path);
        } catch (IOException exception) {
            throw new IllegalStateException("아키텍처 검사 중 소스를 읽을 수 없습니다. path=" + path, exception);
        }
    }
}
