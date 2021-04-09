package tech.tongyu.bct.reference.api;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.reference.dto.CompanyTypeInfoDTO;
import tech.tongyu.bct.reference.service.CompanyTypeInfoService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CompanyTypeInfoApi {
    @Autowired
    private CompanyTypeInfoService companyTypeInfoService;

    @BctMethodInfo(description = "保存交易对手分类信息")
    public void refCompanyTypeInfoBatchCreate(
            @BctMethodArg List<Map<String, Object>> infos
    ) {
        if (infos == null){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "交易对手分类信息不可为空");
        }
        if (infos.size() == 0){
            companyTypeInfoService.deleteAll();
        }else {
            List<CompanyTypeInfoDTO> dtos  = infos.stream().map(v->{
                return JsonUtils.mapper.convertValue(v, CompanyTypeInfoDTO.class);
            }).collect(Collectors.toList());
            companyTypeInfoService.batchUpdateAll(dtos);
        }
    }
}
