package Learning.Management.System.repository;

import Learning.Management.System.entity.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AnnouncementRepo extends JpaRepository<Announcement, Long> {
    boolean existsByAuthorIdAndTitle(Long authorId, String title);

    @Query("select a from Announcement a where a.course is null or exists "
            + "(select e.id from Enrollment e where e.course = a.course and e.student.id = :learnerId)")
    Page<Announcement> findVisibleToLearner(@Param("learnerId") Long learnerId, Pageable pageable);
}
