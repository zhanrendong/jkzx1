package tech.tongyu.bct.rpc.json.http.server.manager;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.auth.dto.SysLogDTO;
import tech.tongyu.bct.common.api.response.RpcResponseListPaged;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.rpc.json.http.server.dao.SysLogRepo;
import tech.tongyu.bct.rpc.json.http.server.dao.entity.SysLogDbo;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SysLogManager{

    private SysLogRepo sysLogRepo;

    @Autowired
    public SysLogManager(SysLogRepo sysLogRepo){
        this.sysLogRepo = sysLogRepo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void save(SysLogDTO sysLog) {
        SysLogDbo sysLogDbo = new SysLogDbo();
        BeanUtils.copyProperties(sysLog,sysLogDbo);
        sysLogDbo.setParams(JsonUtils.mapper.valueToTree(sysLog.getParams()));
        sysLogDbo.setCreatedDateAt(LocalDate.now());
        sysLogRepo.save(sysLogDbo);
    }

    @Transactional(rollbackFor = Exception.class)
    public Optional<SysLogDTO> findByUsername(String username) {
        return sysLogRepo.findByUsername(username).map(this::toDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    public RpcResponseListPaged<SysLogDTO> getSysLogList(Integer page, Integer pageSize, String username,
                                                         String operation, LocalDate startDate, LocalDate endDate) {
        Pageable pageable = PageRequest.of(page, pageSize, new Sort(Sort.Direction.DESC, "createTime"));

        Page<SysLogDbo> sysLogDbos = sysLogRepo.findAll((Root<SysLogDbo> root,
                                                         CriteriaQuery<?> query,
                                                         CriteriaBuilder criteriaBuilder) ->{
            Predicate predicate = criteriaBuilder.conjunction();
            predicate.getExpressions().add(criteriaBuilder.between(root.<LocalDate>get("createdDateAt"), startDate, endDate));
            if (StringUtils.isNotEmpty(username)){
                predicate.getExpressions().add(criteriaBuilder.equal(root.<String>get("username"), username));
            }
            if (StringUtils.isNotEmpty(operation)){
                predicate.getExpressions().add(criteriaBuilder.equal(root.<String>get("operation"), operation));
            }
            return predicate;
        }, pageable);

        List<SysLogDTO> sysLogDTOS = sysLogDbos.getContent()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return new RpcResponseListPaged<>(sysLogDTOS, sysLogDbos.getTotalElements());
    }

    SysLogDTO toDTO(SysLogDbo sysLogDbo){
        SysLogDTO dto = new SysLogDTO();
        BeanUtils.copyProperties(sysLogDbo,dto);
        dto.setUuid(sysLogDbo.getId());
        dto.setCreatedAt(LocalDateTime.ofInstant(
                sysLogDbo.getCreateTime().toInstant(),
                ZoneId.systemDefault()));
        return dto;
    }

    private LocalDate toLocalDate(Date date) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return localDateTime.toLocalDate();
    }

    private Boolean isDateBetween(LocalDate date, LocalDate startDate, LocalDate endDate){
        return !(date.isAfter(endDate) || date.isBefore(startDate));
    }

}
