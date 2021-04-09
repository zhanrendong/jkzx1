package tech.tongyu.bct.workflow.initialize;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.common.util.FileUtils;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.dev.DevLoadedTest;
import tech.tongyu.bct.workflow.dto.ProcessDTO;
import tech.tongyu.bct.workflow.dto.ProcessPersistenceDTO;
import tech.tongyu.bct.workflow.dto.TaskNode;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;
import tech.tongyu.bct.workflow.initialize.process.ProcessDefinitionConstants;
import tech.tongyu.bct.workflow.process.WorkflowProcessConfig;
import tech.tongyu.bct.workflow.process.enums.FilterTypeEnum;
import tech.tongyu.bct.workflow.process.enums.TaskTypeEnum;
import tech.tongyu.bct.workflow.process.manager.ProcessInstanceManager;
import tech.tongyu.bct.workflow.process.manager.TaskNodeManager;
import tech.tongyu.bct.workflow.process.manager.self.*;
import tech.tongyu.bct.workflow.process.repo.ProcessFilterRepo;
import tech.tongyu.bct.workflow.process.repo.TaskNodeRepo;
import tech.tongyu.bct.workflow.process.repo.entities.FilterDbo;
import tech.tongyu.bct.workflow.process.repo.entities.ProcessDbo;
import tech.tongyu.bct.workflow.process.repo.entities.ProcessFilterDbo;
import tech.tongyu.bct.workflow.process.repo.entities.TaskNodeDbo;

import java.util.*;
import java.util.stream.Collectors;

import static tech.tongyu.bct.workflow.initialize.process.ProcessDefinitionConstants.*;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
@Configuration
@ComponentScan(basePackageClasses = WorkflowInitializeConfig.class)
@Import(WorkflowProcessConfig.class)
public class WorkflowInitializeConfig implements DevLoadedTest {

    private FilterManager filterManager;
    private ProcessPersistenceManager processPersistenceManager;
    private RequestManager requestManager;
    private ProcessConfigManager processConfigManager;
    private TaskNodeManager taskNodeManager;
    private ProcessFilterRepo processFilterRepo;


    @Autowired
    public WorkflowInitializeConfig(FilterManager filterManager
            , RequestManager requestManager
            , ProcessConfigManager processConfigManager
            , ProcessPersistenceManager processPersistenceManager
            , TaskNodeManager taskNodeManager
            , ProcessFilterRepo processFilterRepo){
        this.filterManager = filterManager;
        this.requestManager = requestManager;
        this.processConfigManager = processConfigManager;
        this.processPersistenceManager = processPersistenceManager;
        this.taskNodeManager = taskNodeManager;
        this.processFilterRepo = processFilterRepo;
    }

    @Order(1)
    @Bean
    CommandLineRunner processInitialize(){
        // 初始化流程数据，主要是当第一次启动流程服务的时候将一些配置文件中的数据写入到数据库中
        return new CommandLineRunner() {
            @Override
            @Transactional(rollbackFor = Exception.class)
            public void run(String... args) {

                String propertyJson = FileUtils.readClassPathFile("process_definition.json");
                Map<String, Object> processDefinitionJson = JsonUtils.fromJson(propertyJson);

                List<Map<String, Object>> temp = (List<Map<String, Object>>)processDefinitionJson.get(ProcessDefinitionConstants.PROCESS_DEFINITIONS);
                temp = Objects.isNull(temp) ? Lists.newArrayList() : temp;
                for (Map<String, Object> map : temp) {
                    String processName = map.get(PROCESS_NAME).toString();
                    if (!processPersistenceManager.hasProcess(processName)) {
                        processPersistenceManager.saveProcess(processName, false);
                        ProcessPersistenceDTO processPersistenceDTO = processPersistenceManager.getProcessPersistenceDTOByProcessName(processName);
                        List<Map<String, Object>> taskNode = (List<Map<String, Object>>) map.get(TASK_NODE);
                        Collection<TaskNode> collect = taskNode.stream().map(task -> ((TaskNode)
                                new TaskNodeDbo(processPersistenceDTO.getId()
                                        , task.get(NODE_NAME).toString()
                                        , TaskTypeEnum.of(task.get(NODE_TYPE).toString())
                                        ,(Integer) task.get(SEQUENCE)
                                        , task.get(ACTION_CLASS).toString())
                                )
                        ).collect(Collectors.toList());
                        taskNodeManager.createTaskNode(
                                processPersistenceDTO.getId(),
                                collect
                        );
                        List<Map<String, String>> requests = (List<Map<String, String>>) map.get(REQUEST);
                        requests.stream().forEach(request -> {
                            requestManager.saveRequest(
                                    processPersistenceDTO.getId(),
                                    request.get(SERVICE),
                                    request.get(METHOD)
                            );
                        });
                        List<Map<String, String>> configs = (List<Map<String, String>>) map.get(CONFIG);
                        configs.stream().forEach(global -> {
                            processConfigManager.createProcessConfig(
                                    global.get(CONFIG_NAME),
                                    global.get(CONFIG_NICK_NAME),
                                    processName
                            );
                        });
                        List<Map<String, String>> processFilters = (List<Map<String, String>>) map.get(PROCESS_FILTERS);
                        processFilters.stream().forEach(filter -> {
                            FilterTypeEnum filterType = FilterTypeEnum.of(filter.get(FILTER_TYPE));
                            FilterDbo filterDbo = filterManager.saveFilter(filter.get(FILTER_NAME), filter.get(FILTER_CLASS), filterType);
                            TaskTypeEnum taskTypeEnum = TaskTypeEnum.of(filter.get(TASK_TYPE));
                            ProcessFilterDbo processFilterDbo = new ProcessFilterDbo(processPersistenceDTO.getId(), filterDbo.getId(), taskTypeEnum);
                            processFilterRepo.save(processFilterDbo);
                        });
                    }
                }
            }
        };
    }
}