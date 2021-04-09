package tech.tongyu.bct.auth.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.auth.dao.entity.IpTokenBindDbo;

import java.util.Optional;

public interface IpTokenBindRepo extends JpaRepository<IpTokenBindDbo, String> {
    Optional<IpTokenBindDbo> findByIpaddr(String ip);
}
