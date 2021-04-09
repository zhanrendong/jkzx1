package tech.tongyu.bct.acl;

import java.util.List;

public class ResourceAuthentication {

    /*@SuppressWarnings("unchecked")
    public static List<Boolean> can(RestClient restClient, UserInfo userInfo, List<String> resourceName, ResourceTypeEnum resourceType, ResourcePermissionTypeEnum resourcePermission){
        RpcSuccessResponse rpcSuccessResponse = (RpcSuccessResponse) restClient.postTemplate(ServiceEnum.AUTH_SERVICE, new Bean()
                .put(RpcConstants.JSON_RPC_REQUEST_METHOD, "authCan")
                .put(RpcConstants.JSON_RPC_REQUEST_PARAMS, new Bean()
                        .put("resourceType", resourceType.name())
                        .put("resourceName", resourceName)
                        .put("resourcePermission", resourcePermission.name())
                )
        , userInfo);

        return (List<Boolean>) rpcSuccessResponse.getResult();
    }*/
}
