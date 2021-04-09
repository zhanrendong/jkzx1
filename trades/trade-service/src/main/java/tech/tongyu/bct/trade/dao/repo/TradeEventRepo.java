package tech.tongyu.bct.trade.dao.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.tongyu.bct.trade.dao.dbo.TradeEvent;

import java.util.UUID;

@Repository
public interface TradeEventRepo extends JpaRepository<TradeEvent, UUID> {


}
