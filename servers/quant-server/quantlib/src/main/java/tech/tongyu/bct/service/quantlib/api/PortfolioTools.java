package tech.tongyu.bct.service.quantlib.api;

import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApi;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApi2;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApiArg;
import tech.tongyu.bct.service.quantlib.common.enums.CalcType;
import tech.tongyu.bct.service.quantlib.common.enums.PricerType;
import tech.tongyu.bct.service.quantlib.financial.instruments.Portfolio;
import tech.tongyu.bct.service.quantlib.financial.instruments.Position;
import tech.tongyu.bct.service.quantlib.market.curve.Discount;
import tech.tongyu.bct.service.quantlib.market.vol.ImpliedVolSurface;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class PortfolioTools {
    @BctQuantApi(
            name = "qlPortfolioCreate",
            description = "Create a portfolio from positions",
            argDescriptions = {"A list of positions"},
            argNames = {"positions"},
            argTypes = {"ArrayHandle"},
            retName = "portfolio",
            retDescription = "A portfolio",
            retType = "Handle"
    )
    public static Portfolio portfolioCreate(Object[] positions) {
        Portfolio ret = new Portfolio();
        for (int i = 0; i < positions.length; ++i) {
            ret.add((Position<?>) positions[i]);
        }
        return ret;
    }

    @BctQuantApi(
            name = "qlPositionCreate",
            description = "Create a position from a product and its traded quantity",
//            argNames = {"product", "quantity", "trade_id"},
            argNames = {"product", "quantity", "position_id"},
            argTypes = {"Handle", "Double", "String"},
//            argDescriptions = {"The traded product", "the traded quantity", "Trade id"},
            argDescriptions = {"The traded product", "the traded quantity", "Position id"},
            retName = "position",
            retType = "Handle",
            retDescription = "A new position",
            addIdInput = false
    )
    public static Position<?> positionCreate(Object product, double quantity, String id) {
        return new Position<>(product, quantity, id);
    }

    @BctQuantApi(
            name = "qlPortfolioCalcBlack",
            description = "Computes portfolio level price and greeks",
            argNames = {"request", "portfolio", "val", "spot", "vol", "r", "q"},
            argDescriptions = {"Request", "The portfolio to price", "Valuation date", "Spot",
                    "vol", "r", "q"},
            argTypes = {"Enum", "Handle", "DateTime", "Double", "Double", "Double", "Double"},
            retName = "portfolioValue", retType = "Double", retDescription = "Portfolio value"
    )
    public static double portfolioCalcBlack(CalcType request, Portfolio portfolio,
                                            LocalDateTime val, double spot, double vol, double r, double q)
            throws Exception {
        double ret = 0.0;
        int n = portfolio.getPositions().size();
        for (int i = 0; i < n; ++i) {
            Position pos = portfolio.getPositions().get(i);
            ret += OptionPricerBlack.calc(request, pos.getProduct(), val, spot, vol, r, q) * pos.getQuantity();
        }
        return ret;
    }

    @BctQuantApi2(
            name = "qlPortfolioCalcPDE",
            description = "Computes portfolio level price using PDE",
            args = {@BctQuantApiArg(name = "portfoliio", type = "Handle",
            description = "The portfolio to price"),
                    @BctQuantApiArg(name = "val", type = "DateTime",
                    description = "Valuation date"),
                    @BctQuantApiArg(name = "spot", type = "Double",
                    description = "spot"),
                    @BctQuantApiArg(name = "vol", type = "Double",
                    description = "volatility"),
                    @BctQuantApiArg(name = "r", type = "Double",
                    description = "risk free interest rate"),
                    @BctQuantApiArg(name = "q", type = "Double",
                    description = "annually dividend yield"),
                    @BctQuantApiArg(name = "eps", type = "Double",
                    description = "if > 0, pricerParams ignored; if <=0, use pricerParams instead"),
                    @BctQuantApiArg(name = "alpha", type = "Double",
                    description = "implicit index"),
                    @BctQuantApiArg(name = "pricerParams", type = "Json", required = false,
                    description = "contains: minS, maxS, spotNumSteps, timeNumSpots")},
            retName = "price",
            retType = "Double",
            retDescription = "Value of the portfolio"
    )
    public static double portfolioCalcPDE(Portfolio portfolio,
                                          LocalDateTime val, double spot, double vol, double r, double q,
                                          double eps, double alpha, Map<String, Object> params)
            throws Exception {
        double ret = 0.0;
        int n = portfolio.getPositions().size();
        for (int i = 0; i < n; ++i) {
            Position pos = portfolio.getPositions().get(i);
            ret += OptionPricerPDE.calc(pos.getProduct(), val, spot, r, q, vol, eps, alpha, params) * pos.getQuantity();
        }
        return ret;
    }

    /*@BctQuantApi(
            name = "qlPortfolioCalcBinomialTree",
            description = "Computes portfolio level price using binomial tree",
            argNames = {"portfolio", "val", "spot", "vol", "r", "q", "N"},
            argDescriptions = {"The portfolio to price", "Valuation date", "Spot",
                    "vol", "r", "q", "Number of steps"},
            argTypes = {"Handle", "DateTime", "Double", "Double", "Double", "Double", "Integer"},
            retName = "portfolioValue", retType = "Double", retDescription = "Portfolio value"
    )*/
    public static double portfolioCalcBinomialTree(Portfolio portfolio,
                                                   LocalDateTime val, double spot, double vol, double r, double q, int N)
            throws Exception {
        double ret = 0.0;
        int n = portfolio.getPositions().size();
        for (int i = 0; i < n; ++i) {
            Position pos = portfolio.getPositions().get(i);
            ret += OptionPricerBinomialTree.calc(pos.getProduct(), val, spot, vol, r, q, N) * pos.getQuantity();
        }
        return ret;
    }

    @BctQuantApi2(
            name = "qlPortfolioSingleAssetCalcAdv",
            description = "Single asset pricer that outputs price, greeks and Black parameters",
            args = {
                    @BctQuantApiArg(name = "requests", type = "ArrayEnum",
                            description = "Calculation request"),
                    @BctQuantApiArg(name = "portfolio", type = "Handle",
                            description = "The portfolio to price"),
                    @BctQuantApiArg(name = "valuation_date", type = "DateTime", description = "Valuation date"),
                    @BctQuantApiArg(name = "spot", type = "Double", description = "Underlyer spot", required = false),
                    @BctQuantApiArg(name = "vol_surface", type = "Handle",
                            description = "Vol surface", required = false),
                    @BctQuantApiArg(name = "discount_curve", type = "Handle",
                            description = "Discounting curve", required = false),
                    @BctQuantApiArg(name = "dividend_curve", type = "Handle",
                            description = "Dividend/Borrow curve", required = false),
                    @BctQuantApiArg(name = "pricer", type = "Enum",
                            description = "Pricer", required = false),
                    @BctQuantApiArg(name = "pricer_params", type = "Json",
                            description = "Pricer parameters", required = false)
            },
            retName = "request",
            retDescription = "NPV, greeks and Black parameters used",
            retType = "Json"
    )
    public static Map<String, Double> portfolioSingleAssetReport(
            Object[] requests, Portfolio portfolio, LocalDateTime val, double spot,
            ImpliedVolSurface volSurface, Discount discountCurve, Discount dividendCurve,
            PricerType pricerType, Map<String, Object> pricerParams)
            throws Exception {
        Map<String, Double> ret = new HashMap<>();
        Set<String> requestsStr = new TreeSet<>();
        // initialize return map
        for (Object req: requests) {
            String s = ((CalcType) req).name().toLowerCase();
            ret.put(s, 0.0);
            requestsStr.add(s);
        }
        // run through positions
        int n = portfolio.getPositions().size();
        for (Position pos: portfolio.getPositions()) {
            double quantity = pos.getQuantity();
            Map<String, Double> posRet = OptionPricerGeneric.singleAssetReport(
                    requests, pos.getProduct(), val, spot, volSurface, discountCurve,
                    dividendCurve, pricerType, pricerParams);
            for (String req: requestsStr)
                ret.put(req, ret.get(req) + posRet.get(req) * quantity);
            if (!ret.containsKey("r")) {
                ret.put("r", posRet.get("r"));
                ret.put("q", posRet.get("q"));
                ret.put("vol", posRet.get("vol"));
                ret.put("spot", posRet.get("spot"));
            }
        }
        return ret;
    }

    /*@BctQuantApi(
            name = "qlPortfolioReportBlack",
            description = "Computes price/greeks of a portfolio and lists results for each trade inside",
            argNames = {"requests", "portfolio", "val", "spot", "vol", "r", "q"},
            argDescriptions = {"Requests", "A portfolio", "Valuation date",
                    "Underlying spot", "Volatility", "Risk free rate", "Dividend yield"},
            argTypes = {"ArrayEnum", "Handle", "DateTime", "Double","Double", "Double", "Double"},
            retName = "report",
            retDescription = "A matrix of results",
            retType = "Table"
    )
    public static Map<String, List<Object>> portfolioReportBlack(Object[] requests, Portfolio portfolio,
                                                                 LocalDateTime val, double spot, double vol, double r, double q)
            throws Exception {
        Map<String, List<Object>> report = new HashMap<>();

        List<Object> displayOrder = new ArrayList<>();
        displayOrder.add("id");
        for (int i = 0; i < requests.length; ++i) {
            displayOrder.add(((CalcType)requests[i]).name());
        }
        report.put("display_order", displayOrder);

        List<Object> trades = new ArrayList<>();
        for (int i = 0; i < portfolio.getPositions().size(); ++i) {
            trades.add(portfolio.getPositions().get(i).getId());
        }
        report.put("id", trades);

        for (int i = 0; i < requests.length; ++i) {
            CalcType request = (CalcType)requests[i];
            List<Object> results = new ArrayList<>();
            for (int j = 0; j < portfolio.getPositions().size(); ++j) {
                Position pos = portfolio.getPositions().get(j);
                results.add(OptionPricerBlack.calc(request, pos.getProduct(), val, spot, vol, r, q)
                        * pos.getQuantity());
            }
            report.put(request.name(), results);
        }
        return report;
    }*/

    /*@BctQuantApi(
            name = "qlPortfolioSingleAssetCalcBlack",
            description = "Black portfolio calculator for single asset",
            argNames = {"requests", "portfolio", "vol", "r", "q"},
            argDescriptions = {"Calculation requests", "Portfolio", "Vol surface",
                    "Risk free discount curve", "Dividend/borrow rate curve"},
            argTypes = {"ArrayEnum", "Handle", "Handle", "Handle", "Handle"},
            retName = "requests",
            retDescription = "NPV or greeks",
            retType = "Table"
    )
    public static Map<String, List<Object>> portfolioReportGeneric(Object[] requests, Portfolio portfolio,
                                                                   ImpliedVolSurface vol,
                                                                   Discount r, Discount q) throws Exception {
        Map<String, List<Object>> report = new HashMap<>();

        List<Object> displayOrder = new ArrayList<>();
        displayOrder.add("id");
        for (int i = 0; i < requests.length; ++i) {
            displayOrder.add(((CalcType)requests[i]).name());
        }
        report.put("display_order", displayOrder);

        List<Object> trades = new ArrayList<>();
        for (int i = 0; i < portfolio.getPositions().size(); ++i) {
            trades.add(portfolio.getPositions().get(i).getId());
        }
        report.put("id", trades);

        for (int i = 0; i < requests.length; ++i) {
            CalcType request = (CalcType)requests[i];
            List<Object> results = new ArrayList<>();
            for (int j = 0; j < portfolio.getPositions().size(); ++j) {
                Position pos = portfolio.getPositions().get(j);
                results.add(OptionPricerBlack.genericPricer(request, pos.getProduct(), vol, r, q)
                        * pos.getQuantity());
            }
            report.put(request.name(), results);
        }
        return report;
    }*/
}