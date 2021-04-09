package tech.tongyu.bct.market.service;

import org.springframework.data.domain.Pageable;
import tech.tongyu.bct.market.dto.*;
import tech.tongyu.bct.pricing.common.QuoteFieldLocator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 市场行情服务。该服务提供标的物合约管理，日内{@link InstanceEnum#INTRADAY}/收盘{@link InstanceEnum#CLOSE}行情存储检索。
 * 日内行情指交易时段实时行情。该服务仅保存标的物最新日内行情。即
 * 日内行情保证给定标的物仅返回至多一条记录。
 * <p>
 * 收盘行情指标的物每日收盘/结算等行情。每个标的物每个交易只有一条
 * 记录，但给定标的物，系统保留收盘行情历史记录。
 * <p>
 * 标的物信息按照资产{@link AssetClassEnum}（商品{@link AssetClassEnum#COMMODITY}，股票{@link AssetClassEnum#EQUITY}），
 * 合约种类{@link InstrumentTypeEnum}（现货，期货，指数等）
 * 进行分类。每个合约有系统内唯一的标识代码，同时系统记录标的物
 * 合约乘数，交易所等其他信息。每个合约系统仅保存一条记录。
 *
 * @author Lu Lu
 */
public interface MarketDataService {
    String SCHEMA = "marketDataService";

    /**
     * 按给定条件查询单个合约行情
     *
     * @param instrumentId  合约代码
     * @param instance      日内{@link InstanceEnum#INTRADAY}/收盘{@link InstanceEnum#CLOSE}
     * @param valuationDate 估值日
     * @param quoteTimezone 时区
     * @return 合约行情{@link QuoteDTO}, 可能为空
     */
    Optional<QuoteDTO> getQuote(String instrumentId, InstanceEnum instance,
                                LocalDate valuationDate, ZoneId quoteTimezone);

    /**
     * 返回给定标的物列表最新行情。由于系统仅保留日内最新行情，因此该API不需要提供行情时间戳。
     *
     * @param instrumentIds 合约代码列表
     * @return 给定标的物列表最新行情
     */
    List<QuoteDTO> listLatestQuotes(List<String> instrumentIds);

    /**
     * 返回给定标的物列表给定估值日收盘行情
     *
     * @param instrumentIds 合约代码列表
     * @param valuationDate 估值日
     * @param quoteTimezone 时区
     * @return 给定标的物列表收盘行情
     */
    List<QuoteDTO> listCloseQuotes(List<String> instrumentIds, LocalDate valuationDate, ZoneId quoteTimezone);

    /**
     * 批量获取行情。输入为一组行情描述。
     *
     * @param locators 行情描述。参考{@link QuoteLocator}
     * @return 一组行情
     */
    List<Optional<QuoteDTO>> listQuotes(List<QuoteLocator> locators);

    /**
     * 批量获取行情字段。输入为一组行情字段描述。
     *
     * @param locators 行情描述。参考{@link QuoteFieldLocator}
     * @return 一组行情字段
     */
    Map<QuoteFieldLocator, Double> listQuotesByField(List<QuoteFieldLocator> locators);

    /**
     * 返回给定标的物列表给定估值日行情, 包括日内与收盘
     *
     * @param instrumentIds 标的物列表
     * @param valuationDate 估值日
     * @param quoteTimezone 时区
     * @return 日内与收盘行情
     */
    List<CombinedQuoteDTO> listQuotes(List<String> instrumentIds, LocalDate valuationDate, ZoneId quoteTimezone);

    /**
     * 按给定条件删除单个合约行情
     *
     * @param instrumentId  合约代码
     * @param instance      日内{@link InstanceEnum#INTRADAY}/收盘{@link InstanceEnum#CLOSE}
     * @param valuationDate 估值日
     * @param quoteTimezone 时区
     * @return 被删除合约行情{@link QuoteDTO}, 可能为空
     */
    Optional<QuoteDTO> deleteQuote(String instrumentId, InstanceEnum instance,
                                   LocalDate valuationDate, ZoneId quoteTimezone);

    /**
     * 保存单个合约行情
     *
     * @param instrumentId   合约代码
     * @param instance       日内{@link InstanceEnum#INTRADAY}/收盘{@link InstanceEnum#CLOSE}
     * @param quote          行情字段{@link QuoteFieldEnum}及对应行情
     * @param valuationDate  估值日
     * @param quoteTimestamp 保存时间戳
     * @param quoteTimezone  时区
     * @return 被保存合约行情, 可能为空
     */
    QuoteDTO saveQuote(String instrumentId, InstanceEnum instance,
                       Map<QuoteFieldEnum, Double> quote,
                       LocalDate valuationDate, LocalDateTime quoteTimestamp, ZoneId quoteTimezone);

    /**
     * 批量保存行情
     *
     * @param quotes 批量行情
     * @return 被保存的合约代码
     */
    List<String> saveQuotes(List<QuoteDTO> quotes);

    /**
     * 按给定条件保存单个合约行情
     *
     * @param quoteDTO 合约行情
     * @return 被保存合约行情
     */
    QuoteDTO saveQuote(QuoteDTO quoteDTO);

    /**
     * 按合约代码获取合约相关信息
     *
     * @param instrumentId 合约代码
     * @return 合约信息
     */
    Optional<InstrumentDTO> getInstrument(String instrumentId);

    /**
     * 删除合约
     *
     * @param instrumentId 合约代码
     * @return 被删除合约信息, 可能为空
     */
    Optional<InstrumentDTO> deleteInstrument(String instrumentId);

    /**
     * 创建/修改合约信息。合约信息参考:
     * <ul>
     * <li>商品现货{@link CommoditySpotInfo}</li>
     * <li>商品期货{@link CommodityFuturesInfo}</li>
     * <li>股票{@link EquityStockInfo}</li>
     * <li>股指{@link EquityIndexInfo}</li>
     * <li>股指期货 {@link EquityIndexFuturesInfo}</li>
     * </ul>
     *
     * @param instrumentId       合约代码
     * @param assetClassEnum     资产类型
     * @param instrumentTypeEnum 合约种类
     * @param instrumentInfo     合约信息
     * @return 被保存合约信息, 可能为空
     */
    InstrumentDTO saveInstrument(String instrumentId, AssetClassEnum assetClassEnum, AssetSubClassEnum assetSubClass,
                                 InstrumentTypeEnum instrumentTypeEnum, InstrumentInfo instrumentInfo);

    /**
     * 列出合约信息, 可分页。
     *
     * @param p 分页
     * @return 合约信息列表
     */
    List<InstrumentDTO> listInstruments(Pageable p);

    /**
     * 列出合约信息, 可分页。
     *
     * @param template
     * @return 合约信息列表
     */
    List<InstrumentDTO> listInstrumentsByTemplate(InstrumentDTO template);

    /**
     * 总合约代码数
     *
     * @return 总合约代码数
     */
    long countInstruments();

    /**
     * 列出列出合约信息, 可分页。合约信息, 可分页。
     *
     * @param instrumentIds 合约代码列表
     * @param p             分页
     * @return 合约信息列表
     */
    List<InstrumentDTO> listInstruments(List<String> instrumentIds, Pageable p);

    /**
     * 合约代码数
     *
     * @param instrumentIds 合约代码列表
     * @return 合约代码列表
     */
    long countInstruments(List<String> instrumentIds);

    /**
     * 按资产类型，合约种类列出合约信息, 可分页。
     *
     * @param assetClass     资产类型
     * @param instrumentType 合约种类
     * @param p              分页
     * @return 合约信息列表
     */
    List<InstrumentDTO> listInstruments(AssetClassEnum assetClass, InstrumentTypeEnum instrumentType, Pageable p);

    /**
     * 给定资产类型，合约种类合约代码数
     *
     * @param assetClass     资产类型
     * @param instrumentType 资产类型
     * @return 合约代码数
     */
    long countInstruments(AssetClassEnum assetClass, InstrumentTypeEnum instrumentType);

    /**
     * 根据资产类型搜索以instrumentIdPart开头的Instrument
     *
     * @param instrumentIdPart
     * @param assetClassEnum
     * @return
     */
    List<InstrumentDTO> searchInstruments(String instrumentIdPart, Optional<AssetClassEnum> assetClassEnum);

}
