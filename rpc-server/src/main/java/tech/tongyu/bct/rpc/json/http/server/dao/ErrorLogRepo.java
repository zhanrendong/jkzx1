package tech.tongyu.bct.rpc.json.http.server.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.rpc.json.http.server.dao.entity.ErrorLogDbo;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */

public interface ErrorLogRepo extends JpaRepository<ErrorLogDbo, String> {

    void deleteByCreateTimeBefore(LocalDateTime createTime);

    Page<ErrorLogDbo> findAllByUsernameContainingAndRequestMethodContainingAndCreatedDateAtBetweenOrderByCreateTimeDesc(
            String username, String requestMethod, Pageable pageable, LocalDate startDate, LocalDate endDate);
}
