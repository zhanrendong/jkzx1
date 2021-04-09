package tech.tongyu.bct.trade.service.mock;

import tech.tongyu.bct.common.jpa.MockJpaRepository;
import tech.tongyu.bct.trade.dao.dbo.PositionBook;
import tech.tongyu.bct.trade.dao.repo.PositionBookRepo;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class MockBookRepo extends MockJpaRepository<PositionBook> implements PositionBookRepo {

    public MockBookRepo() {
        super(new LinkedList<>());
    }

    @Override
    public Optional<PositionBook> findByBookName(String bookName) {
        return data.stream().filter(b -> b.getBookName().equals(bookName)).findAny();
    }

    @Override
    public List<PositionBook> findAllByBookNameContaining(String similarBookName) {
        return null;
    }
}
