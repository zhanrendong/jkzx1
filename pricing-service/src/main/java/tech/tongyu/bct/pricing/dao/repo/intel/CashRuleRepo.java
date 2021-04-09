package tech.tongyu.bct.pricing.dao.repo.intel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.pricing.dao.dbo.CashRule;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CashRuleRepo extends JpaRepository<CashRule, UUID> {
    Optional<CashRule> findByPricingEnvironmentId(String pricingEnvironmentId);
    @Modifying
    @Transactional
    List<CashRule> deleteByPricingEnvironmentId(String pricingEnvironmentId);
}
