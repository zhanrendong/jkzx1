package tech.tongyu.bct.rpc.json.http.server.manager;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.auth.dto.ErrorLogDTO;
import tech.tongyu.bct.common.api.response.RpcResponseListPaged;
import tech.tongyu.bct.rpc.json.http.server.dao.ErrorLogRepo;
import tech.tongyu.bct.rpc.json.http.server.dao.entity.ErrorLogDbo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
@Component
public class ErrorLogManager {

    private ErrorLogRepo errorLogRepo;

    @Autowired
    public ErrorLogManager(ErrorLogRepo errorLogRepo) {
        this.errorLogRepo = errorLogRepo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void save(ErrorLogDTO errorLogDTO) {
        ErrorLogDbo errorLogDbo = new ErrorLogDbo();
        BeanUtils.copyProperties(errorLogDTO,errorLogDbo);
        errorLogDbo.setCreatedDateAt(LocalDate.now());
        errorLogRepo.save(errorLogDbo);
    }

    @Transactional(rollbackFor = Exception.class)
    public RpcResponseListPaged<ErrorLogDTO> getErrorLogList(Integer page, Integer pageSize, String username,
                                                             String requestMethod, LocalDate startDate, LocalDate endDate) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<ErrorLogDbo> errorLogDbos =
                errorLogRepo.findAllByUsernameContainingAndRequestMethodContainingAndCreatedDateAtBetweenOrderByCreateTimeDesc(
                        username, requestMethod, pageable, startDate, endDate);

        List<ErrorLogDTO> errorLogDTOS = errorLogDbos.getContent()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return new RpcResponseListPaged<>(errorLogDTOS, errorLogDbos.getTotalElements());
    }

    @Transactional(rollbackFor = Exception.class)
    public void clearErrorLogByCreateTime(LocalDateTime startTime) {
        errorLogRepo.deleteByCreateTimeBefore(startTime);
    }

    private ErrorLogDTO toDTO(ErrorLogDbo errorLogDbo){
        ErrorLogDTO dto = new ErrorLogDTO();
        BeanUtils.copyProperties(errorLogDbo,dto);
        dto.setId(errorLogDbo.getId());
        dto.setCreatedAt(LocalDateTime.ofInstant(
                errorLogDbo.getCreateTime().toInstant(),
                ZoneId.systemDefault()));
        return dto;
    }

    private LocalDateTime toLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    private Boolean isDateBetween(LocalDateTime date, LocalDateTime startDate, LocalDateTime endDate){
        return !(date.isAfter(endDate) || date.isBefore(startDate));
    }
}
