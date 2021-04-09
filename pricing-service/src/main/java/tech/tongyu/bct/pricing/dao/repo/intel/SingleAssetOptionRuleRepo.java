package tech.tongyu.bct.pricing.dao.repo.intel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.pricing.dao.dbo.SingleAssetOptionRule;

import java.util.List;
import java.util.UUID;

public interface SingleAssetOptionRuleRepo extends JpaRepository<SingleAssetOptionRule, UUID> {
    List<SingleAssetOptionRule> findByPricingEnvironmentId(String pricingEnvironmentId);
    @Modifying
    @Transactional
    List<SingleAssetOptionRule> deleteByPricingEnvironmentId(String pricingEnvironmentId);
}
