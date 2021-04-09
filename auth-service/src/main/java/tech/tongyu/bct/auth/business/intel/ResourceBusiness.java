package tech.tongyu.bct.auth.business.intel;

import tech.tongyu.bct.auth.dto.Resource;

public interface ResourceBusiness {

    Resource modifyResource(String resourceId, String resourceName, String parentId);

}
