package tech.tongyu.bct.reference.service;

import tech.tongyu.bct.reference.dto.MarginDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 保证金管理服务
 * @author hangzhi
 */
public interface MarginService {

    String SCHEMA = "MarginService";

    List<MarginDTO> marginSearch(String legalName);

    /**
     * 创建维持保证金。
     * 注：该API主要用于弥补历史遗留数据中， 维持保证金缺失． 如果保证金信息已经存在，那么就跳过．
     * @param partyId　交易对手的ID
     * @param maintenanceMargin
     * @return
     */
    MarginDTO createMargin(UUID partyId, BigDecimal maintenanceMargin);

    /**
     * 获取指定的保证金．
     * @param marginId
     * @return
     */
    Optional<MarginDTO> getMargin(UUID marginId);

    /**
     * 通过accuontId检索搜索保证金信息．
     * @param accountIds
     * @return
     */
    List<MarginDTO> getMargins(List<String> accountIds);

    /**
     * 更新维持保证金
     * @param marginId
     * @param maintenanceMargin
     * @return
     */
    MarginDTO updateMaintenanceMargin(UUID marginId, BigDecimal maintenanceMargin);


    /**
     * 批量更新维持保证金。
     *
     * @param margins map (LegalName, MaintenanceMargin)
     * @return
     */
    List<MarginDTO> updateMaintenanceMargins(Map<String, BigDecimal> margins);
}
