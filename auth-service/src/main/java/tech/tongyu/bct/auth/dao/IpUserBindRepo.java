package tech.tongyu.bct.auth.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.auth.dao.entity.IpUserBindDbo;

import java.util.List;
import java.util.Optional;

public interface IpUserBindRepo extends JpaRepository<IpUserBindDbo, String> {
    Optional<List<IpUserBindDbo>> findAllByIpAndUsername(String ip, String username);
    Optional<List<IpUserBindDbo>> findAllByUsername(String username);
}
