package tech.tongyu.bct.rpc.json.http.server.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import tech.tongyu.bct.rpc.json.http.server.dao.entity.SysLogDbo;

import java.util.Optional;


public interface SysLogRepo extends JpaRepository<SysLogDbo, String>, JpaSpecificationExecutor<SysLogDbo> {

    Optional<SysLogDbo> findByUsername(String username);
}
