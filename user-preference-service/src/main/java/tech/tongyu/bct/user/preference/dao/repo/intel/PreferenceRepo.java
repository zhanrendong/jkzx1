package tech.tongyu.bct.user.preference.dao.repo.intel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import tech.tongyu.bct.user.preference.dao.dbo.Preference;

import java.util.Optional;
import java.util.UUID;


@RepositoryRestResource
public interface PreferenceRepo extends JpaRepository<Preference, UUID> {
    Optional<Preference> findByUserName(String userName);
}
