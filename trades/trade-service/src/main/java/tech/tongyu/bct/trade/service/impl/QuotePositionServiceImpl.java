package tech.tongyu.bct.trade.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.cm.product.iov.CashFlowDirectionEnum;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.product.iov.feature.OptionTypeEnum;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.common.util.TimeUtils;
import tech.tongyu.bct.trade.dao.dbo.QuotePosition;
import tech.tongyu.bct.trade.dao.repo.QuotePositionRepo;
import tech.tongyu.bct.trade.dto.trade.QuotePositionDTO;
import tech.tongyu.bct.trade.dto.trade.QuotePrcDTO;
import tech.tongyu.bct.trade.service.QuotePositionService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QuotePositionServiceImpl implements QuotePositionService {

    private static final String UNDERLYER_INSTRUMENT_ID = "underlyerInstrumentId";

    private static final String EXPIRATION_DATE = "expirationDate";

    private static final String OPTION_TYPE = "optionType";

    private static final String DIRECTION = "direction";

    private static final String PAYMENT_DIRECTION = "paymentDirection";

    private QuotePositionRepo quotePositionRepo;

    @Autowired
    public QuotePositionServiceImpl(QuotePositionRepo quotePositionRepo) {
        this.quotePositionRepo = quotePositionRepo;
    }

    @Override
    public void deleteById(UUID uuid) {
        if (Objects.isNull(uuid)){
            throw new CustomException("请输入历史定价quote唯一标识uuid");
        }
        quotePositionRepo.deleteById(uuid);
    }

    @Override
    @Transactional
    public void save(QuotePrcDTO quotePrcDto) {
        String quotePrcId = quotePrcDto.getQuotePrcId();
        if (StringUtils.isBlank(quotePrcId)){
            quotePrcDto.setQuotePrcId(generateQuotePrcId());
        }else{
            if (quotePositionRepo.existsByQuotePrcId(quotePrcId)){
                throw new CustomException(String.format("[%s]试定价唯一标识已经存在,请重新输入", quotePrcId));
            }
        }
        String pricingEnvironmentId = quotePrcDto.getPricingEnvironmentId();
        if (StringUtils.isBlank(quotePrcDto.getPricingEnvironmentId())){
            throw new CustomException("请输入定价环境pricingEnvironmentId");
        }
        quotePrcDto.getQuotePositions().forEach(positionDto -> {
            QuotePosition quotePosition = transToDbo(positionDto);
            quotePosition.setPricingEnvironmentId(pricingEnvironmentId);
            quotePosition.setCounterPartyCode(quotePrcDto.getCounterPartyCode());
            quotePosition.setQuotePrcId(quotePrcDto.getQuotePrcId());
            quotePosition.setUserName(quotePrcDto.getUserName());
            quotePosition.setComment(quotePrcDto.getComment());
            quotePositionRepo.save(quotePosition);
        });
    }

    @Override
    public void deleteByQuotePrcId(String quotePrcId) {
        if (StringUtils.isBlank(quotePrcId)){
            throw new CustomException("请输入试定价唯一标识quotePrcId");
        }
        quotePositionRepo.deleteByQuotePrcId(quotePrcId);
    }

    @Override
    public List<QuotePrcDTO> search(QuotePositionDTO quoteSearchDto, String comment,
                                    LocalDate expirationStartDate, LocalDate expirationEndDate){
        QuotePosition quotePosition = transToDbo(quoteSearchDto);
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withIgnoreCase();
        List<QuotePosition> quotePositions = quotePositionRepo.findAll(Example.of(quotePosition, exampleMatcher));
        if (StringUtils.isNotBlank(comment)){
            quotePositions = quotePositions.stream()
                    .filter(position -> StringUtils.isNotBlank(position.getComment())
                            && position.getComment().contains(comment))
                    .collect(Collectors.toList());
        }
        if (Objects.nonNull(expirationStartDate) && Objects.nonNull(expirationEndDate)){
            quotePositions = quotePositions.stream()
                    .filter(position -> TimeUtils.isDateBetween(position.getExpirationDate(), expirationStartDate, expirationEndDate))
                    .collect(Collectors.toList());
        }
        List<String> quotePrcIds = quotePositions.stream()
                .map(QuotePosition::getQuotePrcId)
                .distinct()
                .collect(Collectors.toList());

        return transToQuotePrcDto(quotePositionRepo.findByQuotePrcIdIn(quotePrcIds));
    }


    private String generateQuotePrcId(){
        synchronized (QuotePositionServiceImpl.class){
            return String.valueOf(System.currentTimeMillis());
        }
    }

    private List<QuotePrcDTO> transToQuotePrcDto(List<QuotePosition> quotePositions){
        return quotePositions.stream()
                .map(QuotePosition::getQuotePrcId)
                .distinct()
                .map(quoteId -> {
                    QuotePrcDTO quotePrcDto = new QuotePrcDTO();
                    QuotePosition quotePosition = quotePositions.stream()
                            .filter(position -> quoteId.equals(position.getQuotePrcId()))
                            .findAny()
                            .get();
                    BeanUtils.copyProperties(quotePosition, quotePrcDto);
                    List<QuotePositionDTO> positions = quotePositions.stream()
                            .filter(position -> quoteId.equals(position.getQuotePrcId()))
                            .map(this::transToDto)
                            .collect(Collectors.toList());
                    quotePrcDto.setQuotePositions(positions);
                    return quotePrcDto;
                }).collect(Collectors.toList());
    }

    private QuotePositionDTO transToDto(QuotePosition quotePosition){
        UUID uuid = quotePosition.getUuid();
        QuotePositionDTO quotePositionDto = new QuotePositionDTO();
        BeanUtils.copyProperties(quotePosition, quotePositionDto);
        quotePositionDto.setUuid(Objects.isNull(uuid) ? null : uuid.toString());
        quotePositionDto.setCreatedAt(TimeUtils.instantTransToLocalDateTime(quotePosition.getCreatedAt()));
        quotePositionDto.setUpdatedAt(TimeUtils.instantTransToLocalDateTime(quotePosition.getUpdatedAt()));
        return quotePositionDto;
    }

    private QuotePosition transToDbo(QuotePositionDTO quotePositionDto){
        String uuid = quotePositionDto.getUuid();
        QuotePosition quotePosition = new QuotePosition();
        BeanUtils.copyProperties(quotePositionDto, quotePosition);

        JsonNode asset = quotePositionDto.getAsset();
        if (Objects.nonNull(asset)) {
            Map<String, Object> assetMap = JsonUtils.mapper.convertValue(asset, Map.class);
            String underlyerInstrumentId = (String) assetMap.get(UNDERLYER_INSTRUMENT_ID);
            String expirationDate = (String) assetMap.get(EXPIRATION_DATE);
            String optionType = (String) assetMap.get(OPTION_TYPE);
            String direction = (String) assetMap.get(DIRECTION);
            String paymentDirection = (String) assetMap.get(PAYMENT_DIRECTION);

            quotePosition.setDirection(StringUtils.isBlank(direction) ? null : InstrumentOfValuePartyRoleTypeEnum.valueOf(direction));
            quotePosition.setExpirationDate(StringUtils.isBlank(expirationDate) ? null : LocalDate.parse(expirationDate));
            quotePosition.setOptionType(StringUtils.isBlank(optionType) ? null : OptionTypeEnum.valueOf(optionType));
            quotePosition.setUuid(StringUtils.isBlank(uuid) ? null : UUID.fromString(uuid));
            quotePosition.setUnderlyerInstrumentId(underlyerInstrumentId);
            quotePosition.setPaymentDirection(StringUtils.isBlank(paymentDirection) ? null : CashFlowDirectionEnum.valueOf(paymentDirection));
        }
        return quotePosition;
    }

}
