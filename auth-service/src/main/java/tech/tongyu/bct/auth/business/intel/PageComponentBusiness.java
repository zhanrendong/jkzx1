package tech.tongyu.bct.auth.business.intel;


import tech.tongyu.bct.auth.dto.PageComponent;
import tech.tongyu.bct.auth.dto.PagePermissionDTO;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface PageComponentBusiness {

    PageComponent authPageComponentList();

    void setPagePermission(Collection<Map<String, Object>> permissions);

    void initializePageComponent(Collection<Map<String, Object>> pages);

    Set<Map<String, Object>> listPagePermission();

    Collection<String> listPagePermissionByRoleId(String roleId);
}
