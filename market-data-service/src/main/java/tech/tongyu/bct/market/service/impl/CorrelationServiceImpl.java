package tech.tongyu.bct.market.service.impl;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.market.dao.dbo.Correlation;
import tech.tongyu.bct.market.dao.repo.intel.CorrelationRepo;
import tech.tongyu.bct.market.dto.CorrelationDTO;
import tech.tongyu.bct.market.service.CorrelationService;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CorrelationServiceImpl implements CorrelationService {
    private CorrelationRepo correlationRepo;

    @Autowired
    public CorrelationServiceImpl(CorrelationRepo correlationRepo) {
        this.correlationRepo = correlationRepo;
    }

    private Tuple2<String, String> orderInstruments(String instrumentId1, String instrumentId2) {
        return instrumentId1.compareToIgnoreCase(instrumentId2) < 0 ?
                Tuple.of(instrumentId1, instrumentId2) : Tuple.of(instrumentId2, instrumentId1);
    }

    @Override
    public Optional<CorrelationDTO> correlation(String instrumentId1, String instrumentId2) {
        Tuple2<String, String> ordered = orderInstruments(instrumentId1, instrumentId2);
        return correlationRepo.findByInstrumentId1AndInstrumentId2(ordered._1, ordered._2)
                .map(c -> new CorrelationDTO(c.getInstrumentId1(), c.getInstrumentId2(), c.getCorrelation()));
    }

    @Override
    public List<List<Double>> correlationMatrix(List<String> instrumentIds) {
        List<List<Double>> m = new ArrayList<>();
        int n = instrumentIds.size();
        for (int i = 0; i < n; ++i) {
            List<Double> row = new ArrayList<>(Collections.nCopies(n, 0.));
            row.set(i, 1.);
            for (int j = i + 1; j < instrumentIds.size(); ++j) {
                String instrumentId1 = instrumentIds.get(i);
                String instrumentId2 = instrumentIds.get(j);
                double c = correlation(instrumentId1, instrumentId2)
                        .map(CorrelationDTO::getCorrelation)
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSING_ENTITY,
                                String.format("无法找到 %s 和 %s 的相关性系数", instrumentId1, instrumentId2)));
                row.set(j, c);
            }
            for (int j = 0; j < i; ++j) {
                row.set(j, m.get(j).get(i));
            }
            m.add(row);
        }
        return m;
    }

    @Override
    @Transactional
    public CorrelationDTO save(String instrumentId1, String instrumentId2, double correlation) {
        if (correlation > 1. || correlation < -1.) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    String.format("相关性系数必须在-1与+1之间。输入为 %s", correlation));
        }
        Tuple2<String, String> ordered = orderInstruments(instrumentId1, instrumentId2);
        Correlation corr = new Correlation(ordered._1, ordered._2, correlation);
        correlationRepo.findByInstrumentId1AndInstrumentId2(ordered._1, ordered._2)
                .ifPresent(c -> corr.setUuid(c.getUuid()));
        Correlation saved = correlationRepo.save(corr);
        return new CorrelationDTO(saved.getInstrumentId1(), saved.getInstrumentId2(), saved.getCorrelation());
    }

    @Override
    @Transactional
    public List<CorrelationDTO> delete(String instrumentId1, String instrumentId2) {
        Tuple2<String, String> ordered = orderInstruments(instrumentId1, instrumentId2);
        return correlationRepo.deleteByInstrumentId1AndInstrumentId2(ordered._1, ordered._2).stream()
                .map(c -> new CorrelationDTO(c.getInstrumentId1(), c.getInstrumentId2(), c.getCorrelation()))
                .collect(Collectors.toList());
    }
}
