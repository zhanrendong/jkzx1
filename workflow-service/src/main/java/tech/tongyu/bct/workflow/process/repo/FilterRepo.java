package tech.tongyu.bct.workflow.process.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.tongyu.bct.workflow.process.repo.entities.FilterDbo;

import java.util.Collection;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
public interface FilterRepo extends JpaRepository<FilterDbo, String> {

    /**
     * list all valid filter with given filter id list
     * @param filterId -> list of filters' id
     * @return -> collection of filters
     */
    @Query(value = "from FilterDbo f where f.id in (?1) and f.revoked = false ")
    Collection<FilterDbo> findValidFilterDbosByFilterId(Collection<String> filterId);
}
