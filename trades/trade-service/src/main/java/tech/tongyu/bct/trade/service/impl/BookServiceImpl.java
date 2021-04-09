package tech.tongyu.bct.trade.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.cm.trade.Book;
import tech.tongyu.bct.trade.dao.dbo.PositionBook;
import tech.tongyu.bct.trade.dao.repo.PositionBookRepo;
import tech.tongyu.bct.trade.dto.trade.BookDTO;
import tech.tongyu.bct.trade.service.BookService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {
    PositionBookRepo bookRepo;

    @Autowired
    public BookServiceImpl(PositionBookRepo positionBookRepo) {
        this.bookRepo = positionBookRepo;
    }

    @Override
    public void createBook(String bookName) {
        Optional<PositionBook> optional = bookRepo.findByBookName(bookName);
        if (optional.isPresent()){
            throw new IllegalArgumentException("bookName已经被占用");
        }
        PositionBook book = new PositionBook(bookName);
        bookRepo.save(book);
    }

    @Override
    public List<BookDTO> listBooks() {
        List<PositionBook> books = bookRepo.findAll();
        return books.stream()
                .map(b -> new BookDTO(b.getUuid().toString(), b.getBookName()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<BookDTO> getBookByName(String bookName) {
        return bookRepo.findByBookName(bookName)
                .map(b -> new BookDTO(b.getUuid().toString(), b.getBookName()));
    }

    @Override
    public List<String> listBySimilarBookName(String similarBookName) {
        return bookRepo.findAllByBookNameContaining(similarBookName)
                .stream()
                .map(PositionBook::getBookName)
                .collect(Collectors.toList());
    }
}
