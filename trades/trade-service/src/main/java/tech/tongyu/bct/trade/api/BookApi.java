package tech.tongyu.bct.trade.api;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.auth.dto.ResourceDTO;
import tech.tongyu.bct.auth.service.ResourceService;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.trade.dto.trade.BookDTO;
import tech.tongyu.bct.trade.service.BookService;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookApi {

    @Autowired
    BookService bookService;

    @Autowired
    ResourceService resourceService;

    @BctMethodInfo(
            description = "生成一个交易簿",
            retDescription = "操作是否成功",
            retName = "true or false",
            service = "trade-service"
    )
    public Boolean trdBookCreate(
            @BctMethodArg(description = "交易簿名称") String bookName
    ) {
        if (StringUtils.isBlank(bookName)) {
            throw new IllegalArgumentException(String.format("請輸入交易簿名称bookName"));
        }
        bookService.createBook(bookName);
        return true;
    }

    @BctMethodInfo(
            description = "获取所有可读的交易簿信息",
            retDescription = "所有可读的交易簿信息",
            retName = "List of BookDTOs",
            returnClass = BookDTO.class,
            service = "trade-service"
    )
    public List<BookDTO> trdBookList() {
        Collection<ResourceDTO> readableBook = resourceService.authBookGetCanRead();
        return readableBook.stream()
                .map(resourceDTO -> new BookDTO(null, resourceDTO.getResourceName())).collect(Collectors.toList());
    }

    @BctMethodInfo(
            description = "交易簿模糊搜索查询",
            retDescription = "符合条件的可读的交易簿信息",
            retName = "List of Book names",
            service = "trade-service"
    )
    public List<String> trdBookListBySimilarBookName(
            @BctMethodArg(description = "模糊查询关键词") String similarBookName
    ) {
        Collection<ResourceDTO> readableBook = resourceService.authBookGetCanRead();
        return readableBook.stream()
                .filter(resource -> resource.getResourceName().contains(similarBookName))
                .map(resourceDTO -> resourceDTO.getResourceName()).collect(Collectors.toList());
    }
}
