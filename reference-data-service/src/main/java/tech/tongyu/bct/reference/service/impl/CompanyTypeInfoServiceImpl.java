package tech.tongyu.bct.reference.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.reference.dao.dbo.CompanyTypeInfo;
import tech.tongyu.bct.reference.dao.repl.intel.CompanyTypeInfoRepo;
import tech.tongyu.bct.reference.dto.CompanyTypeInfoDTO;
import tech.tongyu.bct.reference.service.CompanyTypeInfoService;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CompanyTypeInfoServiceImpl implements CompanyTypeInfoService {
    @Autowired
    private CompanyTypeInfoRepo companyTypeInfoRepo;

    @Override
    public void deleteAll() {
        companyTypeInfoRepo.deleteAll();
    }

    @Override
    @Transactional
    public void batchUpdateAll(List<CompanyTypeInfoDTO> infos) {
        companyTypeInfoRepo.deleteAll();
        companyTypeInfoRepo.saveAll(infos.stream().map(this::convert).collect(Collectors.toList()));
    }

    @Override
    public List<CompanyTypeInfoDTO> getAllByLevelTwoType(String levelTwoType) {
        return companyTypeInfoRepo.findAllByLevelTwoType(levelTwoType).stream().map(this::convert).collect(Collectors.toList());
    }

    private CompanyTypeInfo convert(CompanyTypeInfoDTO dto) {
        CompanyTypeInfo info = new CompanyTypeInfo();
        String uuid = dto.getUuid();
        info.setUuid(StringUtils.isBlank(uuid) ? null : UUID.fromString(uuid));
        BeanUtils.copyProperties(dto, info);
        return info;
    }

    private CompanyTypeInfoDTO convert(CompanyTypeInfo info) {
        UUID uuid = info.getUuid();
        CompanyTypeInfoDTO dto = new CompanyTypeInfoDTO();
        BeanUtils.copyProperties(info, dto);
        dto.setUuid(uuid.toString());
        return dto;
    }
}
