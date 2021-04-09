package tech.tongyu.bct.trade.service;

import org.junit.Test;
import tech.tongyu.bct.trade.dao.repo.PositionBookRepo;
import tech.tongyu.bct.trade.dto.trade.BookDTO;
import tech.tongyu.bct.trade.service.impl.BookServiceImpl;
import tech.tongyu.bct.trade.service.mock.MockBookRepo;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertTrue;

public class BookTest {
    PositionBookRepo bookRepo = new MockBookRepo();
    BookService bookService = new BookServiceImpl(bookRepo);


    @Test
    public void testCreateAndGet(){
        bookService.createBook("book1");
        Optional<BookDTO> bookOptional = bookService.getBookByName("book1");
        assertTrue(bookOptional.isPresent());
    }


    @Test
    public void testFindAll(){
        for (int i = 0; i < 5; i++) {
            bookService.createBook("book" + i );
        }
        List<BookDTO> bookDTOS = bookService.listBooks();
        assertTrue(!bookDTOS.isEmpty());
        assertTrue(bookDTOS.size() == 5);
    }


}
