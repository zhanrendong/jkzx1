package tech.tongyu.bct.document.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.document.dao.dbo.PositionDocument;
import tech.tongyu.bct.document.dao.dbo.TradeDocument;
import tech.tongyu.bct.document.dao.repl.intel.PositionDocumentRepo;
import tech.tongyu.bct.document.dao.repl.intel.TradeDocumentRepo;
import tech.tongyu.bct.document.dto.DocProcessStatusEnum;
import tech.tongyu.bct.document.dto.PositionDocumentDTO;
import tech.tongyu.bct.document.dto.TradeDocumentDTO;
import tech.tongyu.bct.document.service.TradeDocumentService;
import tech.tongyu.bct.reference.dto.PartyDTO;
import tech.tongyu.bct.reference.service.PartyService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TradeDocumentServiceImpl implements TradeDocumentService {

    private PartyService partyService;

    private TradeDocumentRepo tradeDocRepo;

    private PositionDocumentRepo positionDocRepo;

    @Autowired
    public TradeDocumentServiceImpl(PartyService partyService,
                                    TradeDocumentRepo tradeDocRepo,
                                    PositionDocumentRepo positionDocRepo) {
        this.partyService = partyService;
        this.tradeDocRepo = tradeDocRepo;
        this.positionDocRepo = positionDocRepo;
    }

    @Override
    public TradeDocumentDTO createTradeDoc(TradeDocumentDTO tradeDocumentDto) {
        String tradeId = tradeDocumentDto.getTradeId();
        tradeDocRepo.findByTradeId(tradeId).ifPresent(v -> {
            throw new CustomException(String.format("交易确认书已经存在,重复操作,交易编号:%s", tradeId));
        });
        TradeDocument tradeDocument = transToDbo(tradeDocumentDto);
        tradeDocument.setDocProcessStatus(DocProcessStatusEnum.UN_PROCESSED);

        PartyDTO party = partyService.getByLegalName(tradeDocumentDto.getPartyName());
        tradeDocument.setTradeEmail(party.getTradeEmail());
        return transToDto(tradeDocRepo.save(tradeDocument));
    }

    @Override
    public PositionDocumentDTO createPositionDoc(PositionDocumentDTO positionDocumentDto) {
        String positionId = positionDocumentDto.getPositionId();
        positionDocRepo.findByPositionId(positionId).ifPresent(v -> {
            throw new CustomException(String.format("结算通知书已经存在,重复操作,持仓编号:%s", positionId));
        });
        PositionDocument positionDocument = transToDbo(positionDocumentDto);
        positionDocument.setDocProcessStatus(DocProcessStatusEnum.UN_PROCESSED);

        PartyDTO party = partyService.getByLegalName(positionDocumentDto.getPartyName());
        positionDocument.setTradeEmail(party.getTradeEmail());
        return transToDto(positionDocRepo.save(positionDocument));
    }

    @Override
    @Transactional
    public TradeDocumentDTO updateTradeDocStatus(String tradeId, DocProcessStatusEnum docProcessStatus) {
        if (StringUtils.isBlank(tradeId)){
            throw new CustomException("请输入交易编号tradeId");
        }
        TradeDocument tradeDocument = tradeDocRepo.findByTradeId(tradeId)
                .orElseThrow(() -> new CustomException(String.format("交易确认书不存在,交易编号:%s", tradeId)));
        tradeDocument.setDocProcessStatus(docProcessStatus);
        tradeDocument.setUpdatedAt(Instant.now());

        return transToDto(tradeDocRepo.save(tradeDocument));
    }

    @Override
    @Transactional
    public PositionDocumentDTO updatePositionDocStatus(String positionId, DocProcessStatusEnum docProcessStatus) {
        if (StringUtils.isBlank(positionId)){
            throw new CustomException("请输入持仓编号positionId");
        }
        PositionDocument positionDocument = positionDocRepo.findByPositionId(positionId)
                .orElseThrow(() -> new CustomException(String.format("结算通知书不存在,持仓编号:%s", positionId)));

        positionDocument.setDocProcessStatus(docProcessStatus);
        return transToDto(positionDocRepo.save(positionDocument));
    }

    @Override
    public List<String> findPositionIdByTradeId(String tradeId) {
        if (StringUtils.isBlank(tradeId)){
            throw new CustomException("请输入交易编号tradeId");
        }
        return positionDocRepo.findByTradeId(tradeId)
                .stream()
                .map(PositionDocument::getPositionId)
                .collect(Collectors.toList());
    }

    @Override
    public List<TradeDocumentDTO> tradeDocSearch(TradeDocumentDTO tradeDocDto, LocalDate startDate, LocalDate endDate) {
        TradeDocument tradeDocument = transToDbo(tradeDocDto);
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withIgnoreCase();
        List<TradeDocument> tradeDocs = tradeDocRepo.findAll(Example.of(tradeDocument, exampleMatcher));

        return tradeDocs.stream()
                .filter(tradeDoc -> isDateBetween(tradeDoc.getTradeDate(), startDate, endDate))
                .map(this::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PositionDocumentDTO> positionDocSearch(PositionDocumentDTO positionDocDto, LocalDate startDate, LocalDate endDate) {
        PositionDocument positionDocument = transToDbo(positionDocDto);
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withIgnoreCase();
        List<PositionDocument> positionDocs = positionDocRepo.findAll(Example.of(positionDocument, exampleMatcher));

        return positionDocs.stream()
                .filter(positionDoc -> isDateBetween(positionDoc.getExpirationDate(), startDate, endDate))
                .map(this::transToDto)
                .collect(Collectors.toList());
    }

    private Boolean isDateBetween(LocalDate date, LocalDate startDate, LocalDate endDate){
        return !(date.isAfter(endDate) || date.isBefore(startDate));
    }

    private TradeDocumentDTO transToDto(TradeDocument tradeDocument){
        UUID uuid = tradeDocument.getUuid();
        TradeDocumentDTO tradeDocumentDto = new TradeDocumentDTO();
        BeanUtils.copyProperties(tradeDocument, tradeDocumentDto);
        tradeDocumentDto.setUuid(Objects.isNull(uuid) ? null : uuid.toString());

        return tradeDocumentDto;
    }

    private TradeDocument transToDbo(TradeDocumentDTO tradeDocumentDto){
        String uuid = tradeDocumentDto.getUuid();
        TradeDocument tradeDocument = new TradeDocument();
        BeanUtils.copyProperties(tradeDocumentDto, tradeDocument);
        tradeDocument.setUuid(StringUtils.isBlank(uuid) ? null : UUID.fromString(uuid));

        return tradeDocument;
    }

    private PositionDocumentDTO transToDto(PositionDocument positionDocument){
        UUID uuid = positionDocument.getUuid();
        PositionDocumentDTO positionDocumentDto = new PositionDocumentDTO();
        BeanUtils.copyProperties(positionDocument, positionDocumentDto);
        positionDocumentDto.setUuid(Objects.isNull(uuid) ? null : uuid.toString());

        return positionDocumentDto;
    }

    private PositionDocument transToDbo(PositionDocumentDTO positionDocumentDto){
        String uuid = positionDocumentDto.getUuid();
        PositionDocument positionDocument = new PositionDocument();
        BeanUtils.copyProperties(positionDocumentDto, positionDocument);
        positionDocument.setUuid(Objects.isNull(uuid) ? null : UUID.fromString(uuid));

        return positionDocument;
    }


}
