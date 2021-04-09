package tech.tongyu.bct.trade.dao.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.trade.dao.dbo.Portfolio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioRepo extends JpaRepository<Portfolio, UUID> {

    void deleteByPortfolioName(String portfolioName);

    Boolean existsByPortfolioName(String portfolioName);

    Optional<Portfolio> findByPortfolioName(String portfolioName);

    List<Portfolio> findByPortfolioNameContaining(String similarPortfolioName);

}
