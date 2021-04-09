package tech.tongyu.bct.workflow.process.manager.self;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.workflow.process.enums.FilterTypeEnum;
import tech.tongyu.bct.workflow.process.manager.Converter;
import tech.tongyu.bct.workflow.process.repo.FilterRepo;
import tech.tongyu.bct.workflow.process.repo.entities.FilterDbo;

import java.util.Collection;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
@Component
public class FilterManager {

    private FilterRepo filterRepo;

    @Autowired
    public FilterManager(
            FilterRepo filterRepo){
        this.filterRepo = filterRepo;
    }

    @Transactional(rollbackFor = Exception.class)
    public FilterDbo saveFilter(String filterName, String filterClass, FilterTypeEnum filterTypeEnum){
        FilterDbo filterDbo = new FilterDbo(filterName, filterClass, filterTypeEnum);
        return filterRepo.save(filterDbo);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteAll(){
        filterRepo.deleteAll();
    }
}
