package tech.tongyu.bct.auth.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.auth.dao.entity.PageComponentDbo;

import java.util.Collection;
import java.util.Optional;

public interface PageComponentRepo extends JpaRepository<PageComponentDbo, String> {

    @Query("from PageComponentDbo pc where pc.revoked = false")
    Collection<PageComponentDbo> findValidPageComponent();

    @Query("from PageComponentDbo pc where pc.revoked = false and id in (?1)")
    Collection<PageComponentDbo> findValidPageComponentByIds(Collection<String> ids);

    @Transactional
    @Query("from PageComponentDbo pc where pc.revoked = false and pc.pageName = ?1")
    Optional<PageComponentDbo> findValidPageComponentByPageName(String pageName);

    @Query("from PageComponentDbo pc where pc.revoked = false and pc.id = ?1")
    Optional<PageComponentDbo> findValidPageComponentById(String id);

}
