package tech.tongyu.bct.workflow.process.manager;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.workflow.dto.IndexDTO;
import tech.tongyu.bct.workflow.process.trigger.Index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
@Component
public class IndexManager implements ApplicationContextAware {

    private Map<String, Index> map;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        map = applicationContext.getBeansOfType(Index.class);
    }

    public Map<String, Index> getIndexMap(){
        return map;
    }

    public IndexDTO getIndexDTO(String indexBeanName){
        Index index = map.get(indexBeanName);
        return new IndexDTO(index.getIndexName(), indexBeanName);
    }

    public Index getIndex(String indexBeanName){
        Index index = map.get(indexBeanName);
        return index;
    }

    public Collection<IndexDTO> listIndex(){
        List<IndexDTO> indexDTOs = new ArrayList<>();
        map.forEach((indexBeanName, index) -> {
            IndexDTO indexDTO = new IndexDTO(index.getIndexName(), indexBeanName);
            indexDTOs.add(indexDTO);
        });
        return indexDTOs;
    }
}
