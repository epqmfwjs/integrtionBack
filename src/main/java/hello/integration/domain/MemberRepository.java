package hello.integration.domain;

import hello.integration.repository.JoinResponsDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByNickname(String nickname);
    Optional<Member> findByNickname(String nickname);

    Optional<Member> findByNicknameAndPassword(String nickname, String password);
}
