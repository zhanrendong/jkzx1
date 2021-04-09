package tech.tongyu.bct.service.quantlib.common.numerics.ad;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.util.FastMath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.DoubleUnaryOperator;

/**
 * Implements the reverse automatic differentiation algorithm for 1st and 2nd order derivatives in the paper
 * Hessan Matrices via Automatic Differentiation by RM Gower and MP Mello
 * <p>
 * The goal of automatic differentiation is to allow the user to write a function valuation only and automatically
 * obtain its derivatives without working out the function's derivatives either analytically or numerically.
 * The whole process is managed by Backwarder for reverse differentiation. This is efficient when the function
 * is a scalar function of many independent variables. After the reverse valuation, all the 1st and 2nd
 * derivatives with respect to all independent variables are automatically known.
 * Usage:
 * <p>
 * <ul>
 * <li>Declare all the independent variables by their names and values. This is done at the same time
 * the differentiator is created.</li>
 * <li>Build the expression to be differentiated by combining more fundamental simpler functions</li>
 * <li>Call the {@code rollBack} method to reverse differentiate the expression</li>
 * <li>Get the derivatives wanted</li>
 * </ul>
 * <p>Example:
 * <pre>
 *     <code>
 *          Backwarder calculator = new Backwarder(new String[]{"S", "K", "vol", "tau"}, \
 *              new double[]{1.0, 1.2, 0.2, 1.0}, 2);
 *          String lnMoneyness = calculator.ln(calculator.div("S", "K"));
 *          String sqrtVar = calculator.sqrt(calculator.mul(calculator.power("vol", 2), "tau"));
 *          String dp = calculator.sum(calculator.div(lnMoneyness, sqrtVar), calculator.scale(sqrtVar, 0.5));
 *          String dm = calculator.sub(dp, sqrtVar);
 *          String price = calculator.sub(calculator.mul("S", calculator.ncdf(dp)),
 *          calculator.mul("K", calculator.ncdf(dm)));
 *          calculator.rollBack();
 *          double delta = calculator.getDeriv("S");
 *          double gamma = calculator.getDeriv("S", "S");
 *     </code>
 * </pre>
 *
 * @author Lu Lu
 * @since 2016-06-23
 */
public class Backwarder {
    final static NormalDistribution N = new NormalDistribution();
    int order;
    private ArrayList<Variable> vars;
    private HashMap<String, Integer> varMap;
    private double[][] H;
    private int lastIndep;

    public Backwarder(String[] names, double[] values, int order) throws Exception {
        if (order != 1 && order != 2)
            throw new Exception("Derivative order must be either 1 or 2");
        this.order = order;
        vars = new ArrayList<>(names.length);
        varMap = new HashMap<>();
        for (int i = 0; i < names.length; ++i) {
            vars.add(i, new Variable(names[i], values[i]));
            varMap.put(names[i], i);
        }
        this.lastIndep = names.length - 1;
    }

    private Variable get(String name) {
        Integer idx = varMap.get(name);
        if (idx == null)
            return null;
        else
            return vars.get(idx);
    }

    private void appendVariable(Variable v) {
        int idx = vars.size();
        vars.add(v);
        varMap.put(v.name, idx);
    }

    private void applyOne(String name, Variable x,
                          DoubleUnaryOperator f,
                          DoubleUnaryOperator df,
                          DoubleUnaryOperator df2) {
        double value = f.applyAsDouble(x.value);
        double[] J = new double[]{df.applyAsDouble(x.value)};
        Variable v;
        if (this.order == 1) {
            v = new Variable(name, value, new Variable[]{x}, J);
        } else {
            double[][] H = new double[][]{{df2.applyAsDouble(x.value)}};
            v = new Variable(name, value, new Variable[]{x}, J, H);
        }
        appendVariable(v);
    }

    private void applyTwo(String name, Variable x, Variable y,
                          BiFunction<Double, Double, Double> f,
                          BiFunction<Double, Double, double[]> df,
                          BiFunction<Double, Double, double[][]> df2) {
        double value = f.apply(x.value, y.value);
        double[] J = df.apply(x.value, y.value);
        Variable v;
        if (this.order == 1) {
            v = new Variable(name, value, new Variable[]{x, y}, J);
        } else {
            double[][] H = df2.apply(x.value, y.value);
            v = new Variable(name, value, new Variable[]{x, y}, J, H);
        }
        appendVariable(v);
    }

    public String sum(String x, String y) {
        String name = "(" + x + "_sum_" + y + ")";
        applyTwo(name, get(x), get(y),
                (u, v) -> u + v,
                (u, v) -> {
                    return new double[]{1.0, 1.0};
                },
                (u, v) -> {
                    return new double[][]{{0.0, 0.0}, {0.0, 0.0}};
                });
        return name;
    }

    public String sum(String x, double c) {
        String name = "(" + x + "_sum_" + c + ")";
        applyOne(name, get(x), u -> u + c, u -> 1.0, u -> 0.0);
        return name;
    }

    public String sub(String x, String y) {
        String name = "(" + x + "_sub_" + y + ")";
        applyTwo(name, get(x), get(y),
                (u, v) -> u - v,
                (u, v) -> {
                    return new double[]{1.0, -1.0};
                },
                (u, v) -> {
                    return new double[][]{{0.0, 0.0}, {0.0, 0.0}};
                }
        );
        return name;
    }

    public String mul(String x, String y) {
        String name = "(" + x + "_mul_" + y + ")";
        applyTwo(name, get(x), get(y),
                (u, v) -> u * v,
                (u, v) -> {
                    return new double[]{v, u};
                },
                (u, v) -> {
                    return new double[][]{{0.0, 1.0}, {1.0, 0.0}};
                });

        return name;
    }

    public String scale(String x, double c) {
        String name = "(" + x + "_mul_" + c + ")";
        applyOne(name, get(x), u -> u * c, u -> c, u -> 0.0);
        return name;
    }

    public String div(String x, String y) {
        String name = "(" + x + "_div_" + y + ")";
        applyTwo(name, get(x), get(y),
                (u, v) -> u / v,
                (u, v) -> {
                    return new double[]{1.0 / v, -u / (v * v)};
                },
                (u, v) -> {
                    return new double[][]{{0.0, -1.0 / (v * v)}, {-1.0 / (v * v), 2.0 * u / (v * v * v)}};
                });
        return name;
    }

    public String reciprocal(String x) {
        String name = "(1_div_" + x + ")";
        applyOne(name, get(x), u -> 1.0 / u, u -> -1.0 / (u * u), u -> 2.0 / (u * u * u));
        return name;
    }

    public String power(String x, int n) {
        String name = "(" + x + "_power_" + n + ")";
        applyOne(name, get(x),
                u -> FastMath.pow(u, n),
                u -> n * FastMath.pow(u, n - 1),
                u -> n * (n - 1) * FastMath.pow(u, n - 2));
        return name;
    }

    public String sqrt(String x) {
        String name = "sqrt(" + x + ")";
        applyOne(name, get(x), u -> FastMath.sqrt(u), u -> 0.5 / FastMath.sqrt(u), u -> -0.25 / FastMath.pow(u, 1.5));
        return name;
    }

    public String ln(String x) {
        String name = "ln(" + x + ")";
        applyOne(name, get(x), u -> FastMath.log(u), u -> 1.0 / u, u -> -1.0 / (u * u));
        return name;
    }

    public String ncdf(String x) {
        String name = "ncdf(" + x + ")";
        applyOne(name, get(x), u -> N.cumulativeProbability(u), u -> N.density(u), u -> -u * N.density(u));
        return name;
    }

    public String npdf(String x) {
        String name = "npdf(" + x + ")";
        applyOne(name, get(x), u -> N.density(u), u -> -u * N.density(u), u -> (1.0 - u * u) * N.density(u));
        return name;
    }

    public String exp(String x) {
        String name = "exp(" + x + ")";
        applyOne(name, get(x), u -> FastMath.exp(u), u -> FastMath.exp(u), u -> FastMath.exp(u));
        return name;
    }

    public String sin(String x) {
        String name = "sin(" + x + ")";
        applyOne(name, get(x), u -> Math.sin(u), u -> Math.cos(u), u -> -Math.sin(u));
        return name;
    }

    public String cos(String x) {
        String name = "cos(" + x + ")";
        applyOne(name, get(x), u -> Math.cos(u), u -> -Math.sin(u), u -> -Math.cos(u));
        return name;
    }

    public String sinh(String x) {
        String name = "sinh(" + x + ")";
        applyOne(name, get(x), u -> Math.sinh(u), u -> Math.cosh(u), u -> Math.sinh(u));
        return name;
    }

    public String cosh(String x) {
        String name = "cosh(" + x + ")";
        applyOne(name, get(x), u -> Math.cosh(u), u -> Math.sinh(u), u -> Math.cosh(u));
        return name;
    }


    /**
     * Implements Algorithm 4 in the paper for backward differentiation of 1st order derivatives
     */
    private void rollBack1stOrder() {
        int i = vars.size() - 1;
        Variable v = vars.get(i);
        v.v_bar = 1.0;
        for (; i > this.lastIndep; --i) {
            v = vars.get(i);
            for (int j = 0; j < v.deps.length; ++j) {
                v.deps[j].v_bar += v.v_bar * v.J[j];
            }
        }
    }

    /**
     * Implements Algorithm 7 in the paper for backward differentiation of 2nd order derivatives
     */
    private void rollBack2ndOrder() {
        int n = vars.size();
        int i = n - 1;
        Variable v = vars.get(i);
        v.v_bar = 1.0;
        this.H = new double[n][n];
        for (; i > this.lastIndep; --i) {
            v = vars.get(i);
            // pushing
            for (int p = i; p >= 0; --p) {
                if (H[p][i] == 0.0)
                    continue;
                if (p == i) {
                    for (int l = 0; l < v.deps.length; ++l) {
                        for (int m = 0; m < v.deps.length; ++m) {
                            int j = varMap.get(v.deps[l].name);
                            int k = varMap.get(v.deps[m].name);
                            H[j][k] += H[i][i] * v.J[l] * v.J[m];
                        }
                    }
                } else {
                    for (int l = 0; l < v.deps.length; ++l) {
                        int j = varMap.get(v.deps[l].name);
                        if (j == p) {
                            H[p][p] += 2.0 * v.J[l] * H[p][i];
                        } else {
                            H[j][p] += v.J[l] * H[p][i];
                            H[p][j] = H[j][p];
                        }
                    }
                }
            }
            // creating
            for (int l = 0; l < v.deps.length; ++l) {
                for (int m = 0; m < v.deps.length; ++m) {
                    int j = varMap.get(v.deps[l].name);
                    int k = varMap.get(v.deps[m].name);
                    H[j][k] += v.v_bar * v.H[m][l];
                }
            }
            // adjoint
            for (int l = 0; l < v.deps.length; ++l) {
                v.deps[l].v_bar += v.v_bar * v.J[l];
            }
        }
    }

    public void rollBack() {
        if (this.order == 1)
            rollBack1stOrder();
        else
            rollBack2ndOrder();
    }

    public double getValue(String x) {
        return get(x).value;
    }

    public double getDeriv(String x) {
        Variable v = get(x);
        return v.v_bar;
    }

    public double getDeriv(String x, String y) {
        return this.H[varMap.get(x)][varMap.get(y)];
    }
}