package Learning.Management.System.repository;

import Learning.Management.System.model.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnnouncementRepo extends JpaRepository<Announcement, Long> {

}
