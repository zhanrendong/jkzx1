package tech.tongyu.bct.trade.dao.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.trade.dao.dbo.PositionBook;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PositionBookRepo extends JpaRepository<PositionBook, UUID> {
    Optional<PositionBook> findByBookName(String bookName);

    List<PositionBook> findAllByBookNameContaining(String similarBookName);
}
