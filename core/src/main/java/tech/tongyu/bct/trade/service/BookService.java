package tech.tongyu.bct.trade.service;

import tech.tongyu.bct.trade.dto.trade.BookDTO;

import java.util.List;
import java.util.Optional;

public interface BookService {

    void createBook(String bookName);

    List<BookDTO> listBooks();

    Optional<BookDTO> getBookByName(String bookName);

    List<String> listBySimilarBookName(String similarBookName);

}
