package tech.tongyu.bct.pricing.dao.repo.intel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.pricing.dao.dbo.PricingEnvironment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PricingEnvironmentRepo extends JpaRepository<PricingEnvironment, UUID> {
    Optional<PricingEnvironment> findByPricingEnvironmentId(String pricingEnvironmentId);
    List<PricingEnvironment> findAllByOrderByPricingEnvironmentId();
    @Modifying
    @Transactional
    void deleteByPricingEnvironmentId(String pricingEnvironmentId);
}
