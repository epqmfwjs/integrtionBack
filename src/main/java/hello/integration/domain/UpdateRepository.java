package hello.integration.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UpdateRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findAllByOrderByDateDesc(); // 날짜 기준 내림차순 정렬
    List<Announcement> findByTitleContaining(String keyword); // 제목으로 검색
    List<Announcement> findByDateBetween(Date startDate, Date endDate); // 특정 기간 검색
}
