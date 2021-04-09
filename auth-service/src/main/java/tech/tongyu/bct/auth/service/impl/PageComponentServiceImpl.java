package tech.tongyu.bct.auth.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.auth.business.intel.PageComponentBusiness;
import tech.tongyu.bct.auth.dto.PageComponent;
import tech.tongyu.bct.auth.dto.PageComponentDTO;
import tech.tongyu.bct.auth.dto.PagePermissionDTO;
import tech.tongyu.bct.auth.dto.RolePageComponentDTO;
import tech.tongyu.bct.auth.exception.AuthBlankParamException;
import tech.tongyu.bct.auth.service.ApiParamConstants;
import tech.tongyu.bct.auth.service.PageComponentService;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.util.CollectionUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Service
public class PageComponentServiceImpl implements PageComponentService {

    private PageComponentBusiness pageComponentBusiness;

    @Autowired
    public PageComponentServiceImpl(
            PageComponentBusiness pageComponentBusiness
    ) {
        this.pageComponentBusiness = pageComponentBusiness;
    }

    @Override
    @BctMethodInfo(
            description = "获取页面树",
            retName = "page tree",
            retDescription = "页面树",
            returnClass = PageComponent.class,
            service = "auth-service"
    )
    public PageComponent authPageComponentList() {
        return pageComponentBusiness.authPageComponentList();
    }

    @Override
    @BctMethodInfo(
            description = "增加页面",
            retName = "success or failure",
            retDescription = "成功增加或失败",
            returnClass = PageComponent.class,
            service = "auth-service"
    )
    public Boolean authPagePermissionSet(
            @BctMethodArg(name = "permissions", description = "角色与页面ID", argClass = RolePageComponentDTO.class) Collection<Map<String, Object>> permissions
    ) {
        if (CollectionUtils.isEmpty(permissions))
            throw new AuthBlankParamException(ApiParamConstants.PERMISSIONS);

        pageComponentBusiness.setPagePermission(permissions);
        return true;
    }

    @BctMethodInfo(
            description = "初始化页面树",
            service = "auth-service"
    )
    public void authPageComponentInitialize(
            @BctMethodArg(name = "pages", description = "初始化页面树", argClass = PageComponentDTO.class) Collection<Map<String, Object>> pages
    ) {
        if (CollectionUtils.isEmpty(pages))
            throw new AuthBlankParamException(ApiParamConstants.PAGES);

        pageComponentBusiness.initializePageComponent(pages);
    }

    @BctMethodInfo(
            description = "get page ids group by role name",
            retName = "role and page component id",
            retDescription = "角色与页面ID",
            returnClass = RolePageComponentDTO.class,
            service = "auth-service"
    )
    public Set<Map<String, Object>> authPagePermissionGet() {
        return pageComponentBusiness.listPagePermission();
    }

    @BctMethodInfo(
            description = "根据角色ID获取页面ID",
            retName = "page component id list",
            retDescription = "页面ID列表",
            service = "auth-service"
    )
    public Collection<String> authPagePermissionGetByRoleId(
            @BctMethodArg(name = ApiParamConstants.ROLE_ID, description = "角色ID")String roleId) {
        if (StringUtils.isEmpty(roleId)) {
            throw new AuthBlankParamException(ApiParamConstants.ROLE_ID); }

        return pageComponentBusiness.listPagePermissionByRoleId(roleId);
    }
}
