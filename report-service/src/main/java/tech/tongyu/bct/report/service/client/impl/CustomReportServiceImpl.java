package tech.tongyu.bct.report.service.client.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.auth.enums.ResourceTypeEnum;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.common.util.TimeUtils;
import tech.tongyu.bct.report.client.dto.CustomReportDTO;
import tech.tongyu.bct.report.client.service.CustomReportService;
import tech.tongyu.bct.report.dao.client.dbo.CustomReport;
import tech.tongyu.bct.report.dao.client.repo.CustomReportRepo;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CustomReportServiceImpl implements CustomReportService {

    private static final String RESOURCE_TYPE = "resourceType";

    private static final String RESOURCE_NAME = "resourceName";

    private CustomReportRepo customReportRepo;

    @Autowired
    public CustomReportServiceImpl(CustomReportRepo customReportRepo) {
        this.customReportRepo = customReportRepo;
    }

    @Override
    public List<String> listReportName() {
        return customReportRepo.findAllReportName();
    }

    @Override
    public void save(CustomReportDTO customReportDto) {
        CustomReport customReport = transToDbo(customReportDto);
        customReportRepo.save(customReport);
    }

    @Override
    @Transactional
    public void deleteAndSaveBatch(List<CustomReportDTO> customReports) {
        CustomReportDTO customReportExample = customReports.stream()
                .findAny()
                .orElseThrow(() -> new CustomException("保存报告数据不存在"));
        customReportRepo.deleteByReportNameAndReportTypeAndValuationDate(customReportExample.getReportName(),
                customReportExample.getReportType(), customReportExample.getValuationDate());
        customReports.forEach(customReportDto -> {
            CustomReport customReport = transToDbo(customReportDto);
            customReportRepo.save(customReport);
        });
    }

    @Override
    public List<CustomReportDTO> search(CustomReportDTO searchReportDto, LocalDate startDate, LocalDate endDate) {
        CustomReport customReport = transToDbo(searchReportDto);
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withIgnoreCase();
        return customReportRepo.findAll(Example.of(customReport, exampleMatcher), Sort.by("createdAt"))
                .stream()
                .filter(report -> TimeUtils.isDateBetween(report.getValuationDate(), startDate, endDate))
                .map(this::transToDto)
                .collect(Collectors.toList());
    }

    private CustomReportDTO transToDto(CustomReport customReport){
        UUID uuid = customReport.getUuid();
        CustomReportDTO customReportDto = new CustomReportDTO();
        BeanUtils.copyProperties(customReport, customReportDto);
        customReportDto.setUuid(Objects.isNull(uuid) ? null : uuid.toString());
        customReportDto.setCreatedAt(TimeUtils.instantTransToLocalDateTime(customReport.getCreatedAt()));

        return customReportDto;
    }

    private CustomReport transToDbo(CustomReportDTO customReportDto){
        String resourceName = null;
        String resourceType = null;
        String uuid = customReportDto.getUuid();
        CustomReport customReport = new CustomReport();
        BeanUtils.copyProperties(customReportDto, customReport);

        Map<String, Object> reportMap = JsonUtils.mapper.convertValue(customReportDto.getReportData(), Map.class);
        if (CollectionUtils.isNotEmpty(reportMap)){
          resourceName = (String) reportMap.get(RESOURCE_NAME);
          resourceType = (String) reportMap.get(RESOURCE_TYPE);
        }
        customReport.setUuid(StringUtils.isBlank(uuid) ? null : UUID.fromString(uuid));
        customReport.setResourceName(StringUtils.isBlank(resourceName) ? null : resourceName);
        customReport.setResourceType(StringUtils.isBlank(resourceType) ? null : ResourceTypeEnum.valueOf(resourceType));

        return customReport;
    }

}
