package com.espay.admincore.adapter.out.persistence.user;

import com.espay.admincore.adapter.out.persistence.user.mapper.UserPersistenceMapper;
import com.espay.admincore.adapter.out.persistence.user.repository.UserJpaRepository;
import com.espay.admincore.adapter.out.persistence.user.repository.UserQueryDslRepository;
import com.espay.admincore.application.dto.user.UserQuery;
import com.espay.admincore.application.port.out.user.UserLookupPort;
import com.espay.admincore.application.port.out.user.UserPersistencePort;
import com.espay.admincore.application.port.out.user.UserSearchPort;
import com.espay.admincore.domain.model.user.AdminUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 조회·저장 포트를 JPA와 QueryDSL 기반 저장소에 연결하는 영속성 어댑터.
 * 단건 및 중복 조회는 Spring Data JPA에, 조건 검색과 페이징은 QueryDSL 저장소에 위임하고
 * {@link UserPersistenceMapper}를 통해 영속성 엔티티가 애플리케이션 밖으로 노출되지 않게 한다.
 */
@Repository
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserLookupPort, UserPersistencePort, UserSearchPort {

    private final UserJpaRepository repository;
    private final UserQueryDslRepository queryRepository;
    private final UserPersistenceMapper mapper;

    /**
     * 외부에서 전달된 문자열 사용자 ID를 DB 식별자 타입으로 변환하여 사용자를 조회한다.
     *
     * @param userId 조회할 사용자 ID
     * @return 일치하는 사용자 도메인 모델 또는 빈 값
     */
    @Override
    public Optional<AdminUser> findById(String userId) {
        return repository.findById(Long.valueOf(userId)).map(mapper::toDomain);
    }

    /**
     * 로그인 인증과 중복 검사에 사용할 사용자를 로그인 ID로 조회한다.
     *
     * @param loginId 조회할 로그인 ID
     * @return 일치하는 사용자 도메인 모델 또는 빈 값
     */
    @Override
    public Optional<AdminUser> findByLoginId(String loginId) {
        return repository.findByLoginId(loginId).map(mapper::toDomain);
    }

    /**
     * OTP 안내 메일 발송 등에 사용할 사용자를 이메일 주소로 조회한다.
     *
     * @param email 조회할 이메일 주소
     * @return 일치하는 사용자 도메인 모델 또는 빈 값
     */
    @Override
    public Optional<AdminUser> findByEmail(String email) {
        return repository.findByEmail(email).map(mapper::toDomain);
    }

    /**
     * 신규 사용자 생성 전에 동일 로그인 ID가 이미 저장되어 있는지 확인한다.
     *
     * @param loginId 중복 여부를 검사할 로그인 ID
     * @return 동일 로그인 ID가 존재하면 {@code true}
     */
    @Override
    public boolean existsByLoginId(String loginId) {
        return repository.existsByLoginId(loginId);
    }

    /**
     * 사용자 도메인 모델을 JPA 엔티티로 변환해 저장하고, 저장 결과를 다시 도메인 모델로 반환한다.
     *
     * @param user 생성하거나 변경할 사용자
     * @return DB 반영 결과가 포함된 사용자 도메인 모델
     */
    @Override
    public AdminUser save(AdminUser user) {
        return mapper.toDomain(repository.save(mapper.toEntity(user)));
    }

    /**
     * 권한 삭제 가능 여부를 판단할 수 있도록 해당 권한을 사용 중인 사용자 수를 조회한다.
     *
     * @param roleId 사용자 수를 계산할 권한 ID
     * @return 해당 권한에 연결된 사용자 수
     */
    @Override
    public long countByRoleId(String roleId) {
        return repository.countByRoleId(Long.valueOf(roleId));
    }

    /**
     * 사용자 검색 조건과 페이지 정보를 QueryDSL 저장소에 전달하여 현재 페이지를 조회한다.
     *
     * @param query 검색어·권한·상태·페이지 조건
     * @return 조건에 맞는 현재 페이지의 사용자 도메인 목록
     */
    @Override
    public List<AdminUser> findPage(UserQuery query) {
        return queryRepository.findPage(query).stream().map(mapper::toDomain).toList();
    }

    /**
     * 사용자 목록과 같은 검색 조건에 맞는 전체 건수를 계산한다.
     *
     * @param query 검색어·권한·상태 조건
     * @return 조건에 맞는 전체 사용자 수
     */
    @Override
    public long count(UserQuery query) {
        return queryRepository.count(query);
    }
}
