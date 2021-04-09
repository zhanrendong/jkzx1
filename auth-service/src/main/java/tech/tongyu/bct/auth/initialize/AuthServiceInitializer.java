package tech.tongyu.bct.auth.initialize;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.auth.dto.*;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.common.util.FileUtils;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.common.util.SystemConfig;
import tech.tongyu.bct.auth.manager.*;
import tech.tongyu.bct.auth.service.impl.PageComponentServiceImpl;
import tech.tongyu.bct.auth.AuthConstants;
import tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum;
import tech.tongyu.bct.auth.enums.ResourceTypeEnum;
import tech.tongyu.bct.auth.enums.UserTypeEnum;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
@Component
public class AuthServiceInitializer {
    
    private UserManager userManager;
    private RoleManager roleManager;
    private ResourceManager resourceManager;
    private ResourcePermissionManager resourcePermissionManager;
    private DepartmentManager departmentManager;
    private PageComponentManager pageComponentManager;

    @Autowired
    public AuthServiceInitializer(
            UserManager userManager
            , RoleManager roleManager
            , ResourceManager resourceManager
            , ResourcePermissionManager resourcePermissionManager
            , DepartmentManager departmentManager
            , PageComponentManager pageComponentManager){
        this.userManager = userManager;
        this.roleManager = roleManager;
        this.resourceManager = resourceManager;
        this.resourcePermissionManager = resourcePermissionManager;
        this.departmentManager = departmentManager;
        this.pageComponentManager = pageComponentManager;
    }

    @Bean
    CommandLineRunner systemInitialize(){
        return (args) -> {
            SystemConfig.put(AuthConstants.EXPIRATION_DAYS, 36500);
            SystemConfig.put(AuthConstants.MAX_LOGIN_FAILURE_TIMES, 8);
        };
    }

    @Order(1)
    @Bean
    CommandLineRunner dbInitialize(){
        return new CommandLineRunner() {
            @Override
            @Transactional(rollbackFor = Exception.class)
            public void run(String... args) {

                String companyName = "上海同余信息科技有限公司";
                if(!departmentManager.hasCompanyInfo()) {
                    departmentManager.createCompanyInfo(companyName, "金融科技", "xxxxxxxxxx"
                            , "金斌", "jinbin@tongyu.tech", companyName);
                }
                DepartmentDTO departmentDto;

                List<String> roleIdList = Lists.newArrayList();
                if(!roleManager.isRoleExist(AuthConstants.ADMIN)) {
                    RoleDTO roleDto = roleManager.createRole(AuthConstants.ADMIN, AuthConstants.ADMIN, AuthConstants.ADMIN);
                    roleIdList.add(roleDto.getId());
                }

                if(!departmentManager.hasRootDepartment()) {
                    departmentDto = departmentManager.createDepartment(companyName, "公司"
                            , companyName, null, 0);
                }else {
                    departmentDto = departmentManager.getDepartmentByDepartmentNameAndParentId(companyName, null);
                }

                if(!userManager.isUserExists(AuthConstants.ADMIN)) {
                    UserDTO userDto = userManager.createUser(AuthConstants.ADMIN, AuthConstants.ADMIN, "yangyiwei@tongyu.tech", "12345"
                            , UserTypeEnum.NORMAL, departmentDto.getId());
                    userManager.updateUserRoles(userDto.getId(), roleIdList);
                }

                if(!resourceManager.isRootResourceExist()){
                    ResourceDTO resourceDto = resourceManager.createRootResource(companyName, departmentDto.getId());
                    departmentManager.linkDepartmentAndResource(departmentDto.getId(), resourceDto.getId());
                    resourcePermissionManager.createResourcePermissions(AuthConstants.ADMIN, companyName, ResourceTypeEnum.NAMESPACE, null
                            , Lists.newArrayList(
                                    ResourcePermissionTypeEnum.CREATE_NAMESPACE
                                    , ResourcePermissionTypeEnum.DELETE_NAMESPACE
                                    , ResourcePermissionTypeEnum.UPDATE_NAMESPACE
                                    , ResourcePermissionTypeEnum.READ_RESOURCE
                                    , ResourcePermissionTypeEnum.GRANT_ACTION
                                    , ResourcePermissionTypeEnum.CREATE_USER
                                    , ResourcePermissionTypeEnum.READ_USER
                                    , ResourcePermissionTypeEnum.UPDATE_USER
                                    , ResourcePermissionTypeEnum.DELETE_USER
                                    , ResourcePermissionTypeEnum.CREATE_INSTRUMENT
                                    , ResourcePermissionTypeEnum.UPDATE_INSTRUMENT
                                    , ResourcePermissionTypeEnum.DELETE_INSTRUMENT
                                    , ResourcePermissionTypeEnum.CREATE_DEPARTMENT
                                    , ResourcePermissionTypeEnum.UPDATE_DEPARTMENT
                                    , ResourcePermissionTypeEnum.DELETE_DEPARTMENT
                                    , ResourcePermissionTypeEnum.LOCK_USER
                                    , ResourcePermissionTypeEnum.UNLOCK_USER
                                    , ResourcePermissionTypeEnum.EXPIRE_USER
                                    , ResourcePermissionTypeEnum.UNEXPIRE_USER
                                    , ResourcePermissionTypeEnum.CHANGE_PASSWORD
                                    , ResourcePermissionTypeEnum.CREATE_ROLE
                                    , ResourcePermissionTypeEnum.DELETE_ROLE
                                    , ResourcePermissionTypeEnum.UPDATE_ROLE
                                    , ResourcePermissionTypeEnum.CREATE_SCRIPT_USER
                            ));
                }

                String marginName = "保证金";
                ResourceDTO rootResource = resourceManager.getRootResource();
                if (!resourceManager.isResourceExist(marginName, ResourceTypeEnum.MARGIN, rootResource.getId())) {
                    resourceManager.createResource(marginName, ResourceTypeEnum.MARGIN, rootResource.getId(), departmentDto.getId(), 0);
                    resourcePermissionManager.createResourcePermissions(AuthConstants.ADMIN, marginName, ResourceTypeEnum.MARGIN, rootResource.getId(),
                            Lists.newArrayList(
                                    ResourcePermissionTypeEnum.GRANT_ACTION
                                    , ResourcePermissionTypeEnum.UPDATE_MARGIN
                            ));
                }

                String clientInfoName = "客户信息";
                if (!resourceManager.isResourceExist(clientInfoName, ResourceTypeEnum.CLIENT_INFO, rootResource.getId())) {
                    resourceManager.createResource(clientInfoName, ResourceTypeEnum.CLIENT_INFO, rootResource.getId(), departmentDto.getId(), 0);
                    resourcePermissionManager.createResourcePermissions(AuthConstants.ADMIN, clientInfoName, ResourceTypeEnum.CLIENT_INFO, rootResource.getId()
                            , Lists.newArrayList(
                                    ResourcePermissionTypeEnum.GRANT_ACTION
                                    , ResourcePermissionTypeEnum.READ_CLIENT
                                    , ResourcePermissionTypeEnum.CREATE_CLIENT
                                    , ResourcePermissionTypeEnum.UPDATE_CLIENT
                                    , ResourcePermissionTypeEnum.DELETE_CLIENT
                            )
                    );
                }

                String groupInfoName = "审批组管理";
                if (!resourceManager.isResourceExist(groupInfoName, ResourceTypeEnum.APPROVAL_GROUP, rootResource.getId())) {
                    resourceManager.createResource(groupInfoName, ResourceTypeEnum.APPROVAL_GROUP, rootResource.getId(), departmentDto.getId(), 0);
                    resourcePermissionManager.createResourcePermissions(AuthConstants.ADMIN, groupInfoName, ResourceTypeEnum.APPROVAL_GROUP, rootResource.getId()
                            , Lists.newArrayList(
                                    ResourcePermissionTypeEnum.GRANT_ACTION
                                    , ResourcePermissionTypeEnum.CREATE_APPROVAL_GROUP
                                    , ResourcePermissionTypeEnum.UPDATE_TASK_NODE
                            )
                    );
                }

                String processName = "流程定义";
                if (!resourceManager.isResourceExist(processName, ResourceTypeEnum.PROCESS_DEFINITION, rootResource.getId())) {
                    ResourceDTO resource = resourceManager.createResource(processName, ResourceTypeEnum.PROCESS_DEFINITION, rootResource.getId(), departmentDto.getId(), 0);
                    resourcePermissionManager.createResourcePermissions(AuthConstants.ADMIN, processName, ResourceTypeEnum.PROCESS_DEFINITION, rootResource.getId()
                            , Lists.newArrayList(
                                    ResourcePermissionTypeEnum.CREATE_PROCESS_AND_TRIGGER
                            )
                    );

                    String resourceName = "触发器管理";
                    resourceManager.createResource(resourceName, ResourceTypeEnum.TRIGGER, resource.getId(), null, 0);
                    resourcePermissionManager.createResourcePermissions(AuthConstants.ADMIN, resourceName, ResourceTypeEnum.TRIGGER, resource.getId()
                            , Lists.newArrayList(
                                    ResourcePermissionTypeEnum.GRANT_ACTION
                                    , ResourcePermissionTypeEnum.CREATE_TRIGGER
                            )
                    );
                    String tradeProcess = "交易录入";
                    resourceManager.createResource(tradeProcess, ResourceTypeEnum.PROCESS_DEFINITION_INFO, resource.getId(), null, 0);
                    resourcePermissionManager.createResourcePermissions(AuthConstants.ADMIN, tradeProcess, ResourceTypeEnum.PROCESS_DEFINITION_INFO, resource.getId()
                            , Lists.newArrayList(
                                    ResourcePermissionTypeEnum.GRANT_ACTION
                                    , ResourcePermissionTypeEnum.UPDATE_PROCESS_DEFINITION
                                    , ResourcePermissionTypeEnum.BIND_PROCESS_TRIGGER
                            )
                    );
                    String creditProcess = "授信额度变更";
                    resourceManager.createResource(creditProcess, ResourceTypeEnum.PROCESS_DEFINITION_INFO, resource.getId(), null, 0);
                    resourcePermissionManager.createResourcePermissions(AuthConstants.ADMIN, creditProcess, ResourceTypeEnum.PROCESS_DEFINITION_INFO, resource.getId()
                            , Lists.newArrayList(
                                    ResourcePermissionTypeEnum.GRANT_ACTION
                                    , ResourcePermissionTypeEnum.UPDATE_PROCESS_DEFINITION
                                    , ResourcePermissionTypeEnum.BIND_PROCESS_TRIGGER
                            )
                    );
                    String fundProcess = "财务出入金";
                    resourceManager.createResource(fundProcess, ResourceTypeEnum.PROCESS_DEFINITION_INFO, resource.getId(), null, 0);
                    resourcePermissionManager.createResourcePermissions(AuthConstants.ADMIN, fundProcess, ResourceTypeEnum.PROCESS_DEFINITION_INFO, resource.getId()
                            , Lists.newArrayList(
                                    ResourcePermissionTypeEnum.GRANT_ACTION
                                    , ResourcePermissionTypeEnum.UPDATE_PROCESS_DEFINITION
                                    , ResourcePermissionTypeEnum.BIND_PROCESS_TRIGGER
                            )
                    );
                    String accountProcess = "开户";
                    resourceManager.createResource(accountProcess, ResourceTypeEnum.PROCESS_DEFINITION_INFO, resource.getId(), null, 0);
                    resourcePermissionManager.createResourcePermissions(AuthConstants.ADMIN, accountProcess, ResourceTypeEnum.PROCESS_DEFINITION_INFO, resource.getId()
                            , Lists.newArrayList(
                                    ResourcePermissionTypeEnum.GRANT_ACTION
                                    , ResourcePermissionTypeEnum.UPDATE_PROCESS_DEFINITION
                                    , ResourcePermissionTypeEnum.BIND_PROCESS_TRIGGER
                            )
                    );
                }
            }
        };
    }

    @Order(5)
    @Bean
    CommandLineRunner pageInit(){
        return new CommandLineRunner() {
            @Override
            @Transactional(rollbackFor = Exception.class)
            public void run(String... args) {
                // read pages.json
                Map<String,Object> map = JsonUtils.fromJson(FileUtils.readClassPathFile("pages.json"));
                // save data
                pageComponentManager.initializePages(null,map);
            }
        };
    }

    @Order(6)
    @Bean
    CommandLineRunner pagePermission(){
        return new CommandLineRunner() {
            @Override
            @Transactional(rollbackFor = Exception.class)
            public void run(String... args) {
                String adminRoleId = roleManager.getRoleByRoleName("admin");
                if(!CollectionUtils.isEmpty(pageComponentManager.listPagePermissionByRoleId(adminRoleId))) {
                    return;
                }
                Collection<String> pageComponentIds = new HashSet<>();
                pageComponentIds.add(pageComponentManager.getPageComponentIdByPageName("systemSettings"));
                pageComponentIds.add(pageComponentManager.getPageComponentIdByPageName("calendars"));
                pageComponentIds.add(pageComponentManager.getPageComponentIdByPageName("riskSettings"));
                pageComponentIds.add(pageComponentManager.getPageComponentIdByPageName("roleManagement"));
                pageComponentIds.add(pageComponentManager.getPageComponentIdByPageName("users"));
                pageComponentIds.add(pageComponentManager.getPageComponentIdByPageName("department"));
                pageComponentIds.add(pageComponentManager.getPageComponentIdByPageName("resources"));
                pageComponentIds.add(pageComponentManager.getPageComponentIdByPageName("tradeBooks"));
                pageComponentIds.add(pageComponentManager.getPageComponentIdByPageName("volatilityCalendar"));
                pageComponentIds.add(pageComponentManager.getPageComponentIdByPageName("approvalProcess"));
                pageComponentIds.add(pageComponentManager.getPageComponentIdByPageName("auditingManagement"));
                pageComponentIds.add(pageComponentManager.getPageComponentIdByPageName("processConfiguration"));
                pageComponentIds.add(pageComponentManager.getPageComponentIdByPageName("operationLog"));
                pageComponentIds.add(pageComponentManager.getPageComponentIdByPageName("documentManagement"));
                pageComponentIds.add(pageComponentManager.getPageComponentIdByPageName("approvalProcessManagement"));
                pageComponentManager.setPagePermissions(adminRoleId,pageComponentIds);
            }
        };
    }

}
