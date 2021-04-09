package tech.tongyu.bct.exchange.service;

import tech.tongyu.bct.exchange.dto.CombStockDTO;

import java.util.List;

public interface CombStockService {
    String SCHEMA = "risk";

    List<CombStockDTO> findCombStock(String bookId, String dealDate);

    void checkCombStock(String bookId, String dealDate, String timezone);
}
