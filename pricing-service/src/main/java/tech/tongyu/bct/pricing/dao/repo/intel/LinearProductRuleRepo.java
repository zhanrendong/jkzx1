package tech.tongyu.bct.pricing.dao.repo.intel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.pricing.dao.dbo.LinearProductRule;

import java.util.List;
import java.util.UUID;

public interface LinearProductRuleRepo extends JpaRepository<LinearProductRule, UUID> {
    List<LinearProductRule> findByPricingEnvironmentId(String pricingEnvironmentId);
    @Modifying
    @Transactional
    List<LinearProductRule> deleteByPricingEnvironmentId(String pricingEnvironmentId);
}
