package tech.tongyu.bct.auth.service;


import tech.tongyu.bct.auth.dto.PageComponent;

import java.util.Collection;
import java.util.Map;

public interface PageComponentService {

    PageComponent authPageComponentList();

    Boolean authPagePermissionSet(Collection<Map<String, Object>> permissions);

}
