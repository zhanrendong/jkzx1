package tech.tongyu.bct.pricing.api.response;

import tech.tongyu.bct.common.api.doc.BctField;
import tech.tongyu.bct.common.api.response.RpcResponseWithDiagnostics;
import tech.tongyu.bct.pricing.common.Diagnostic;

import java.util.List;

public class OnExchangePricingResponse
        implements RpcResponseWithDiagnostics<List<OnExchangeResults>, List<Diagnostic>> {
    @BctField(
            description = "定价结果",
            isCollection = true,
            componentClass = OnExchangeResults.class
    )
    private final List<OnExchangeResults> results;
    @BctField(
            description = "诊断",
            isCollection = true,
            componentClass = Diagnostic.class
    )
    private final List<Diagnostic> diagnostics;

    public OnExchangePricingResponse(List<OnExchangeResults> results, List<Diagnostic> diagnostics) {
        this.results = results;
        this.diagnostics = diagnostics;
    }

    @Override
    public List<OnExchangeResults> getResult() {
        return results;
    }

    @Override
    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }
}
