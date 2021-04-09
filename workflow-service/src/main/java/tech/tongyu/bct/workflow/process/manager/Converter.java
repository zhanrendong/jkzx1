package tech.tongyu.bct.workflow.process.manager;

import com.google.common.collect.Lists;
import org.springframework.beans.BeanUtils;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.workflow.dto.ProcessConfigDTO;
import tech.tongyu.bct.workflow.dto.ProcessPersistenceDTO;
import tech.tongyu.bct.workflow.dto.TaskActionDTO;
import tech.tongyu.bct.workflow.process.repo.entities.FilterDbo;
import tech.tongyu.bct.workflow.process.repo.entities.ProcessConfigDbo;
import tech.tongyu.bct.workflow.process.repo.entities.ProcessDbo;

import java.util.Collection;
import java.util.stream.Collectors;

public class Converter {

    public static tech.tongyu.bct.workflow.dto.FilterDTO convertFilterDbo2Dto(FilterDbo filterDbo){
        tech.tongyu.bct.workflow.dto.FilterDTO filterDTO = new tech.tongyu.bct.workflow.dto.FilterDTO();
        BeanUtils.copyProperties(filterDbo, filterDTO);
        return filterDTO;
    }

    public static Collection<tech.tongyu.bct.workflow.dto.FilterDTO> convertFilterDbo2Dto(Collection<FilterDbo> filterDbo){
        if(CollectionUtils.isEmpty(filterDbo)) {
            return Lists.newArrayList();
        }
        return filterDbo.stream()
                .map(Converter::convertFilterDbo2Dto)
                .collect(Collectors.toList());
    }

    public static ProcessPersistenceDTO convertProcessDbo2Dto(ProcessDbo processDbo){
        ProcessPersistenceDTO processPersistenceDTO = new ProcessPersistenceDTO();
        BeanUtils.copyProperties(processDbo, processPersistenceDTO);
        return processPersistenceDTO;
    }

    public static Collection<ProcessPersistenceDTO> convertProcessDbo2ProcessPersistenceDto(Collection<ProcessDbo> processDbos){
        if(CollectionUtils.isEmpty(processDbos)) {
            return Lists.newArrayList();
        }
        return processDbos.stream()
                .map(Converter::convertProcessDbo2Dto)
                .collect(Collectors.toList());
    }

    public static ProcessConfigDTO convertProcessConfigDbo2ProcessConfigDto(ProcessConfigDbo processConfigDbo){
        return new ProcessConfigDTO(
                processConfigDbo.getId(),
                processConfigDbo.getConfigName(),
                processConfigDbo.getProcessId(),
                processConfigDbo.getStatus()
        );
    }

    public static Collection<ProcessConfigDTO> convertProcessConfigDbo2ProcessConfigDto(Collection<ProcessConfigDbo> processConfigDbos){
        if(CollectionUtils.isEmpty(processConfigDbos)) {
            return Lists.newArrayList();
        }
        return processConfigDbos.stream()
                .map(Converter::convertProcessConfigDbo2ProcessConfigDto)
                .collect(Collectors.toSet());
    }


}
