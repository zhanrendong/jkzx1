package tech.tongyu.bct.reference.dao.repl.intel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.tongyu.bct.reference.dao.dbo.CompanyTypeInfo;

import java.util.List;
import java.util.UUID;

@Repository
public interface CompanyTypeInfoRepo extends JpaRepository<CompanyTypeInfo, UUID> {
    List<CompanyTypeInfo> findAllByLevelTwoType(String levelTwoType);
}
