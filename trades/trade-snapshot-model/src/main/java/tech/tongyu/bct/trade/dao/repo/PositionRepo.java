package tech.tongyu.bct.trade.dao.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.tongyu.bct.trade.dao.dbo.Position;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PositionRepo extends JpaRepository<Position, UUID> {

    void deleteByPositionId(String positionId);

    Optional<Position> findByPositionId(String positionId);

    Optional<List<Position>> findByPositionIdIn(List<String> positionId);

    boolean existsByPositionId(String positionId);
}
