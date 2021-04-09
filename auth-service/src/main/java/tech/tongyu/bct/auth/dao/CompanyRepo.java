package tech.tongyu.bct.auth.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.auth.dao.entity.CompanyDbo;

public interface CompanyRepo extends JpaRepository<CompanyDbo, String> {
}
