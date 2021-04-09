package tech.tongyu.bct.common.api.response;

import java.util.List;

public class RpcResponseListPaged<T> {
    private final List<T> page;
    private final long totalCount;

    public RpcResponseListPaged(List<T> page, long totalCount) {
        this.page = page;
        this.totalCount = totalCount;
    }

    public List<T> getPage() {
        return page;
    }

    public long getTotalCount() {
        return totalCount;
    }
}
