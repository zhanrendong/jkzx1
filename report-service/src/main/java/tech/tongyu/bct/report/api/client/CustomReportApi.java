package tech.tongyu.bct.report.api.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.auth.enums.ResourceTypeEnum;
import tech.tongyu.bct.auth.service.ResourceService;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.api.response.RpcResponseListPaged;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.report.client.dto.CustomReportCollectionDTO;
import tech.tongyu.bct.report.client.dto.CustomReportDTO;
import tech.tongyu.bct.report.client.service.CustomReportService;
import tech.tongyu.bct.report.dto.ReportTypeEnum;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class CustomReportApi {

    private ResourceService resourceService;

    private CustomReportService customReportService;

    @Autowired
    public CustomReportApi(ResourceService resourceService, CustomReportService customReportService) {
        this.resourceService = resourceService;
        this.customReportService = customReportService;
    }

    @BctMethodInfo(
            description = "保存自定义报告",
            retDescription = "是否成功保存",
            retName = "true or false",
            service = "report-service"
    )
    public Boolean rptCustomReportSaveBatch(
            @BctMethodArg(description = "报告名称") String reportName,
            @BctMethodArg(description = "报告类型", argClass = ReportTypeEnum.class) String reportType,
            @BctMethodArg(description = "计算日期") String valuationDate,
            @BctMethodArg(description = "报告数据") List<LinkedHashMap<String, Object>> reports
    ){
        if (StringUtils.isBlank(reportName)){
            throw new CustomException("请输入报告名称reportName");
        }
        if (StringUtils.isBlank(reportType)){
            throw new CustomException("请输入报告类型reportType");
        }
        if (StringUtils.isBlank(valuationDate)){
            throw new CustomException("请输入计算日期valuationDate");
        }
        if (CollectionUtils.isEmpty(reports)){
            throw new CustomException("请输入报告数据reportData");
        }
        List<CustomReportDTO> reportDtoList = reports.stream().map(report -> {
            JsonNode reportData = JsonUtils.mapper.convertValue(report, JsonNode.class);
            CustomReportDTO customReportDto = new CustomReportDTO();
            customReportDto.setReportData(reportData);
            customReportDto.setReportName(reportName);
            customReportDto.setReportType(ReportTypeEnum.valueOf(reportType));
            customReportDto.setValuationDate(LocalDate.parse(valuationDate));
            return customReportDto;
        }).collect(Collectors.toList());
        customReportService.deleteAndSaveBatch(reportDtoList);
        return true;
    }

    @BctMethodInfo(
            description = "获取所有自定义报告名字",
            retDescription = "所有自定义报告名字",
            retName = "List of custom report name",
            service = "report-service"
    )
    public List<String> rptCustomReportNameList(){
        return customReportService.listReportName();
    }

    @BctMethodInfo(
            description = "分页查询自定义报告",
            retDescription = "符合条件的自定义报告",
            retName = "paged CustomReportCollectionDTOs",
            returnClass = CustomReportCollectionDTO.class,
            service = "report-service"
    )
    public RpcResponseListPaged<CustomReportCollectionDTO> rptCustomReportSearchPaged(
            @BctMethodArg(description = "页码") Integer page,
            @BctMethodArg(description = "页距") Integer pageSize,
            @BctMethodArg(description = "结束日期") String endDate,
            @BctMethodArg(description = "开始日期") String startDate,
            @BctMethodArg(required = false, description = "报告名称") String reportName,
            @BctMethodArg(required = false, description = "报告类型", argClass = ReportTypeEnum.class) String reportType
    ){
        if (StringUtils.isBlank(startDate)){
            throw new CustomException("请输入开始时间startDate");
        }
        if (StringUtils.isBlank(endDate)){
            throw new CustomException("请输入结束时间endDate");
        }
        CustomReportDTO searchDto = new CustomReportDTO();
        searchDto.setReportName(StringUtils.isBlank(reportName) ? null : reportName);
        searchDto.setReportType(StringUtils.isBlank(reportType) ? null : ReportTypeEnum.valueOf(reportType));
        List<CustomReportDTO> customReports = customReportService.search(searchDto,
                LocalDate.parse(startDate), LocalDate.parse(endDate));
        List<CustomReportCollectionDTO> customCollections = transToReportCollection(filterByResourceRight(customReports));

        int start = page * pageSize;
        int end = Math.min(start + pageSize, customCollections.size());
        return new RpcResponseListPaged<>(customCollections.subList(start, end), customCollections.size());
    }



    private List<CustomReportCollectionDTO> transToReportCollection(List<CustomReportDTO> customReports){
        return customReports.stream()
                .map(customReport -> new CustomReportCollectionDTO(customReport.getReportName(),
                        customReport.getReportType(), customReport.getValuationDate()))
                .distinct()
                .peek(collection -> {
                    CustomReportDTO customReportDto = customReports.stream()
                            .filter(customReport -> isReportRight(collection, customReport))
                            .findFirst().get();
                    List<JsonNode> reportData = customReports.stream()
                            .filter(customReport -> isReportRight(collection, customReport))
                            .map(CustomReportDTO::getReportData)
                            .collect(Collectors.toList());
                    collection.setUuid(customReportDto.getUuid());
                    collection.setUpdateTime(customReportDto.getCreatedAt());
                    collection.setReportData(reportData);
                }).collect(Collectors.toList());

    }

    private Boolean isReportRight(CustomReportCollectionDTO collection, CustomReportDTO customReport){
        return collection.getReportName().equals(customReport.getReportName()) &&
                collection.getReportType().equals(customReport.getReportType()) &&
                collection.getValuationDate().equals(customReport.getValuationDate());
    }


    private List<CustomReportDTO> filterByResourceRight(List<CustomReportDTO> customReports){
        Map<ResourceTypeEnum, List<String>> resourceMap = new HashMap<>();
        customReports.stream()
                .map(CustomReportDTO::getResourceType)
                .distinct()
                .forEach(resourceType -> {
                    if (Objects.nonNull(resourceType)) {
                        List<String> resourceNames = new ArrayList<>(resourceService.authResourceList(resourceType.toString()));
                        resourceMap.put(resourceType, resourceNames);
                    }
                });
        return customReports.stream()
                .filter(customReport -> isReportCanRead(resourceMap, customReport))
                .collect(Collectors.toList());
    }

    private Boolean isReportCanRead(Map<ResourceTypeEnum, List<String>> resourceMap, CustomReportDTO customReport){
        if (Objects.isNull(customReport.getResourceType())){
            return true;
        }
        if (StringUtils.isBlank(customReport.getResourceName())){
            return true;
        }
        if (CollectionUtils.isEmpty(resourceMap)){
            return false;
        }
        return resourceMap.get(customReport.getResourceType())
                .stream()
                .anyMatch(resourceName -> resourceName.equals(customReport.getResourceName()));
    }
}
