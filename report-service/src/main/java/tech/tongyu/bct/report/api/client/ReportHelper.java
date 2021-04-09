package tech.tongyu.bct.report.api.client;

import org.apache.commons.lang3.StringUtils;
import tech.tongyu.bct.auth.authaction.intel.ResourceAuthAction;
import tech.tongyu.bct.auth.dto.ResourceDTO;
import tech.tongyu.bct.common.api.response.RpcResponseListPaged;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.report.dto.report.HasBookName;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ReportHelper {

    ResourceAuthAction resourceAuthAction;

    public ReportHelper(ResourceAuthAction resourceAuthAction) {
        this.resourceAuthAction = resourceAuthAction;
    }

    public List<String> getReadableBook(boolean strict) {
        List<String> bookIds = resourceAuthAction.getReadableBookAndCanReadTrade().stream().map(ResourceDTO::getResourceName)
                .collect(Collectors.toList());
        if(strict && CollectionUtils.isEmpty(bookIds)){
            throw new CustomException("没有可用的交易簿信息");
        }
        return bookIds;

    }

    public <R extends HasBookName> RpcResponseListPaged<R> filterAndPaginateReport(
            Integer page, Integer pageSize, Supplier<List<R>> func) {
        return paginateReport(page, pageSize, filterReport(func));
    }

    public <R extends HasBookName> List<R> filterReport(Supplier<List<R>> func) {
        List<String> bookIds = getReadableBook(true);
        return func.get().stream().filter(rpt -> bookIds.contains(rpt.getBookName())).collect(Collectors.toList());
    }

    public <R> RpcResponseListPaged<R> paginateReport(Integer page, Integer pageSize, List<R> list) {
        if (!Objects.isNull(page) && !Objects.isNull(pageSize)) {
            int start = page * pageSize;
            int end = Math.min(start + pageSize, list.size());
            return new RpcResponseListPaged<>(list.subList(start, end), list.size());
        } else if (Objects.isNull(page) && Objects.isNull(pageSize)) {
            return new RpcResponseListPaged<>(list, list.size());
        } else {
            throw new IllegalArgumentException("页数和页面数量必须同时为空或者同时不为空");
        }
    }

    public void paramCheck(String param, String errorMsg){
        throwIllegalArgumentException(StringUtils.isBlank(param), errorMsg);
    }

    public void paramCheck(Collection<?> param, String errorMsg){
        throwIllegalArgumentException(CollectionUtils.isEmpty(param), errorMsg);
    }

    public void paramCheck(Map param, String errorMsg){
        throwIllegalArgumentException(CollectionUtils.isEmpty(param), errorMsg);
    }

    private void throwIllegalArgumentException(boolean hasError, String errorMsg){
        if (hasError) {
            throw new IllegalArgumentException(errorMsg);
        }
    }
}
