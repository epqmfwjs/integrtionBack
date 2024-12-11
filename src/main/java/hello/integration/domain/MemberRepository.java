package hello.integration.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByNickname(String nickname);
    Optional<Member> findByNickname(String nickname);

    void deleteByNickname(String nickname);

    boolean existsByCharacterId(int characterId);

//    @Query("SELECT m FROM Member m")
    List<Member> findAll();
}
