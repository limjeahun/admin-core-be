package com.espay.admincore.architecture;

import com.espay.admincore.application.port.out.history.FileHistoryPersistencePort;
import com.espay.admincore.application.port.out.history.LoginHistoryPersistencePort;
import com.espay.admincore.application.port.out.role.RolePersistencePort;
import com.espay.admincore.application.port.out.user.UserPersistencePort;
import com.espay.admincore.domain.model.file.FileHistory;
import com.espay.admincore.domain.model.history.LoginHistory;
import com.espay.admincore.domain.model.role.AdminRole;
import com.espay.admincore.domain.model.user.AdminUser;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 애플리케이션 경계의 Command와 Query 생성 규칙 및 도메인 모델 명명 규칙을 검사한다.
 */
class MethodParameterConventionTest {

    private static final Path CORE_CLASSES = Path.of("build/classes/java/main/com/espay/admincore");
    private static final Path MAIN_SOURCES = Path.of("src/main/java");
    private static final Path TEST_SOURCES = Path.of("src/test/java");
    private static final Pattern PARAMETER_OBJECT_NAME = Pattern.compile(".*(Command|Query|Source)$");

    /**
     * 생성·수정 값을 담기만 하는 일반적인 Data 타입이 도메인 모델로 다시 추가되지 않도록 확인한다.
     *
     * @throws IOException 도메인 소스 목록을 읽지 못한 경우
     */
    @Test
    void domainModelDoesNotUseGenericCreateOrUpdateDataTypes() throws IOException {
        Path domainModel = MAIN_SOURCES.resolve("com/espay/admincore/domain/model");
        List<String> violations;
        try (var sourceFiles = Files.walk(domainModel)) {
            violations = sourceFiles
                    .filter(path -> path.toString().endsWith("CreateData.java")
                            || path.toString().endsWith("UpdateData.java"))
                    .map(Path::toString)
                    .sorted()
                    .toList();
        }
        assertThat(violations).isEmpty();
    }

    /**
     * Aggregate 영속성 Port가 중계 Command 대신 Aggregate Root를 직접 저장하는지 확인한다.
     */
    @Test
    void aggregatePersistencePortsSaveAggregateRootsDirectly() {
        Map<Class<?>, Class<?>> portAggregates = Map.of(
                UserPersistencePort.class, AdminUser.class,
                RolePersistencePort.class, AdminRole.class,
                LoginHistoryPersistencePort.class, LoginHistory.class,
                FileHistoryPersistencePort.class, FileHistory.class
        );

        List<String> violations = portAggregates.entrySet().stream()
                .filter(entry -> {
                    var saveMethods = Arrays.stream(entry.getKey().getDeclaredMethods())
                            .filter(method -> method.getName().equals("save"))
                            .toList();
                    return saveMethods.size() != 1
                            || saveMethods.get(0).getParameterCount() != 1
                            || saveMethods.get(0).getParameterTypes()[0] != entry.getValue()
                            || saveMethods.get(0).getReturnType() != entry.getValue();
                })
                .map(entry -> entry.getKey().getName() + " must save " + entry.getValue().getName())
                .sorted()
                .toList();

        assertThat(violations).isEmpty();
    }

    /**
     * Application Service가 다른 유스케이스의 입력 Port를 주입받아 서비스 간 호출을 만들지 않는지 확인한다.
     *
     * @throws IOException 컴파일된 서비스 클래스 목록을 읽지 못한 경우
     */
    @Test
    void applicationServicesDoNotDependOnInputPorts() throws IOException {
        List<String> violations;
        try (var classFiles = Files.walk(CORE_CLASSES.resolve("application/service"))) {
            violations = classFiles
                    .filter(path -> path.toString().endsWith(".class"))
                    .filter(path -> !path.getFileName().toString().contains("$"))
                    .map(this::className)
                    .map(this::load)
                    .flatMap(type -> Arrays.stream(type.getDeclaredFields()))
                    .filter(field -> field.getType().getPackageName()
                            .startsWith("com.espay.admincore.application.port.in"))
                    .map(field -> field.getDeclaringClass().getName() + "#" + field.getName()
                            + " -> " + field.getType().getName())
                    .sorted()
                    .toList();
        }

        assertThat(violations).isEmpty();
    }

    /**
     * 모든 Application Service가 Spring 표준 Component Scan 대상으로 등록되는지 확인한다.
     *
     * @throws IOException 컴파일된 서비스 클래스 목록을 읽지 못한 경우
     */
    @Test
    void applicationServicesUseServiceStereotype() throws IOException {
        List<String> violations;
        try (var classFiles = Files.walk(CORE_CLASSES.resolve("application/service"))) {
            violations = classFiles
                    .filter(path -> path.toString().endsWith(".class"))
                    .filter(path -> !path.getFileName().toString().contains("$"))
                    .map(this::className)
                    .map(this::load)
                    .filter(type -> !type.isAnnotationPresent(Service.class))
                    .map(Class::getName)
                    .sorted()
                    .toList();
        }

        assertThat(violations).isEmpty();
    }

    /**
     * 트랜잭션 경계가 클래스 전체가 아니라 각 유스케이스 메서드에 직접 선언되는지 확인한다.
     *
     * @throws IOException 컴파일된 서비스 클래스 목록을 읽지 못한 경우
     */
    @Test
    void applicationServicesDoNotUseClassLevelTransactions() throws IOException {
        List<String> violations;
        try (var classFiles = Files.walk(CORE_CLASSES.resolve("application/service"))) {
            violations = classFiles
                    .filter(path -> path.toString().endsWith(".class"))
                    .filter(path -> !path.getFileName().toString().contains("$"))
                    .map(this::className)
                    .map(this::load)
                    .filter(type -> type.isAnnotationPresent(Transactional.class))
                    .map(Class::getName)
                    .sorted()
                    .toList();
        }

        assertThat(violations).isEmpty();
    }

    /**
     * 코어의 Parameter Object가 외부에 통일된 {@code of(...)} 생성 진입점을 제공하는지 확인한다.
     * 호출자가 레코드의 정규 생성자에 직접 결합되지 않게 하여, 이후 검증이나 정규화 로직을
     * 팩터리 내부에 추가하더라도 호출부의 생성 방식이 바뀌지 않도록 보호한다.
     *
     * @throws IOException 컴파일된 클래스 목록을 읽지 못한 경우
     */
    @Test
    void parameterObjectsExposeOfFactories() throws IOException {
        List<String> violations = parameterObjectTypes().stream()
                .filter(type -> Arrays.stream(type.getDeclaredMethods())
                        .noneMatch(method -> method.getName().equals("of")
                                && Modifier.isStatic(method.getModifiers())
                                && method.getReturnType().equals(type)))
                .map(Class::getName)
                .sorted()
                .toList();

        assertThat(violations).isEmpty();
    }

    /**
     * 코어의 Parameter Object가 세 개 이상의 값을 하나의 의미 있는 입력으로 묶는지 확인한다.
     * 한두 개의 값만 전달하는 메서드는 직접 파라미터를 사용하도록 강제하여 의미 없는 래퍼 DTO가
     * 다시 추가되는 것을 방지한다.
     *
     * @throws IOException 컴파일된 클래스 목록을 읽지 못한 경우
     */
    @Test
    void parameterObjectsContainAtLeastThreeValues() throws IOException {
        List<String> violations = parameterObjectTypes().stream()
                .filter(type -> type.getRecordComponents().length < 3)
                .map(type -> type.getName() + " componentCount=" + type.getRecordComponents().length)
                .sorted()
                .toList();

        assertThat(violations).isEmpty();
    }

    /**
     * 코어 Parameter Object의 정규 생성자가 선언 파일 바깥에서 직접 호출되지 않는지 확인한다.
     * 레코드 자체의 {@code of(...)} 구현만 {@code new}를 사용할 수 있으며, 운영 코드와 테스트 코드는
     * 모두 명명된 정적 팩터리를 통해 객체를 생성해야 한다.
     *
     * @throws IOException 소스 파일을 읽지 못한 경우
     */
    @Test
    void parameterObjectsAreCreatedThroughOfFactories() throws IOException {
        List<JavaSource> sources = javaSources();
        List<String> violations = parameterObjectTypes().stream()
                .flatMap(type -> directConstructorCalls(type, sources))
                .sorted()
                .toList();

        assertThat(violations).isEmpty();
    }

    /**
     * 애플리케이션 DTO 중 이름으로 역할이 명확한 레코드 Parameter Object를 찾는다.
     * Command, Query, Source 접미사를 가진 프로젝트 소유 타입만 생성 규칙의 대상으로 삼는다.
     *
     * @return 생성 규칙을 검사할 Parameter Object 타입 목록
     * @throws IOException 컴파일된 클래스 목록을 읽지 못한 경우
     */
    private List<Class<?>> parameterObjectTypes() throws IOException {
        try (var classFiles = Files.walk(CORE_CLASSES)) {
            return classFiles
                    .filter(path -> path.toString().endsWith(".class"))
                    .filter(path -> !path.getFileName().toString().contains("$"))
                    .map(this::className)
                    .filter(name -> name.startsWith("com.espay.admincore.application.dto."))
                    .map(this::load)
                    .filter(Class::isRecord)
                    .filter(type -> PARAMETER_OBJECT_NAME.matcher(type.getSimpleName()).matches())
                    .toList();
        }
    }

    /**
     * 운영 및 테스트 Java 소스를 한 번 읽어 직접 생성자 호출 검사에 사용할 스냅샷으로 만든다.
     * 동일 파일을 각 Parameter Object마다 반복해서 읽지 않도록 경로와 내용을 함께 보관한다.
     *
     * @return 검사할 Java 소스 목록
     * @throws IOException 소스 디렉터리를 순회하거나 파일을 읽지 못한 경우
     */
    private List<JavaSource> javaSources() throws IOException {
        try (var mainSources = Files.walk(MAIN_SOURCES);
             var testSources = Files.walk(TEST_SOURCES)) {
            return Stream.concat(mainSources, testSources)
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(this::readSource)
                    .toList();
        }
    }

    /**
     * 특정 Parameter Object를 선언 파일 밖에서 {@code new}로 생성한 위치를 찾는다.
     * 선언 파일은 {@code of(...)} 구현이 실제 인스턴스를 만들기 위해 정규 생성자를 호출하므로
     * 검사에서 제외한다.
     *
     * @param type 검사할 Parameter Object 타입
     * @param sources 운영 및 테스트 Java 소스
     * @return 파일과 줄 번호가 포함된 위반 위치 스트림
     */
    private Stream<String> directConstructorCalls(Class<?> type, List<JavaSource> sources) {
        Path declaration = MAIN_SOURCES.resolve(type.getName().replace('.', '/') + ".java")
                .toAbsolutePath().normalize();
        Pattern directCall = Pattern.compile("\\bnew\\s+(?:[\\w$]+\\.)*"
                + Pattern.quote(type.getSimpleName()) + "(?:\\s*<[^>]*>)?\\s*\\(");

        return sources.stream()
                .filter(source -> !source.path().toAbsolutePath().normalize().equals(declaration))
                .flatMap(source -> directCall.matcher(source.content()).results()
                        .map(result -> source.path() + ":" + lineNumber(source.content(), result.start())
                                + " -> new " + type.getSimpleName() + "(...)"));
    }

    /**
     * Java 소스 파일을 검사 가능한 경로와 문자열 묶음으로 읽는다.
     * 읽기 실패는 검사 자체를 신뢰할 수 없는 상태이므로 즉시 예외로 변환한다.
     *
     * @param path 읽을 Java 소스 경로
     * @return 경로와 전체 내용을 가진 소스 스냅샷
     */
    private JavaSource readSource(Path path) {
        try {
            return new JavaSource(path, Files.readString(path));
        } catch (IOException exception) {
            throw new IllegalStateException("생성 규칙 검사 중 소스를 읽을 수 없습니다: " + path, exception);
        }
    }

    /**
     * 전체 소스 문자열의 문자 위치를 사람이 확인할 수 있는 1부터 시작하는 줄 번호로 변환한다.
     *
     * @param content 전체 소스 문자열
     * @param position 위반 구문의 시작 문자 위치
     * @return 1부터 시작하는 줄 번호
     */
    private long lineNumber(String content, int position) {
        return content.substring(0, position).chars().filter(character -> character == '\n').count() + 1;
    }

    /**
     * 컴파일 클래스 경로를 JVM의 완전한 클래스명으로 변환한다.
     *
     * @param classFile 변환할 .class 파일 경로
     * @return 패키지가 포함된 클래스명
     */
    private String className(Path classFile) {
        Path relative = Path.of("build/classes/java/main").relativize(classFile);
        return relative.toString().replace('\\', '.').replace('/', '.')
                .replaceFirst("\\.class$", "");
    }

    /**
     * 검사할 클래스를 초기화 없이 현재 테스트 클래스 로더로 읽는다.
     *
     * @param className 완전한 클래스명
     * @return 로드된 클래스
     */
    private Class<?> load(String className) {
        try {
            return Class.forName(className, false, getClass().getClassLoader());
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("메서드 규칙 검사 중 클래스를 읽을 수 없습니다: " + className, exception);
        }
    }

    /**
     * 생성자 호출 검사를 위해 Java 소스의 경로와 전체 내용을 함께 보관한다.
     *
     * @param path 소스 파일 경로
     * @param content 소스 파일 전체 내용
     */
    private record JavaSource(Path path, String content) {
    }
}
