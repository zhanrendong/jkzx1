package tech.tongyu.bct.service.quantlib.common.numerics.ad;


/**
 * Represents a variable that can either be an independent variable or a function of other variables.
 * In the notation of the paper Hessian Matrices via Automatic Differentiation, independent variables are
 * \phi_(1-n), ..., \phi_0, and the dependent variables are \phi_1, ..., \phi_n.
 * Each variable must have a name, its dependent variables, its value, its Jacobian and optionally its Hessian matrix.
 * If the variable is independent, its Jacobian is just the scalar 1 and its Hessian is null.
 * Otherwise the Jacobian is a vector consisting of its partial derivatives.
 * The Hessian matrix is an optional input. It is required only when 2nd order derivatives are computed.
 * Upon construction of a variable, all the above inputs should be already known. The construction of
 * the expression to be differentiated is implicitly a forward valuation process and therefore intermediate
 * function values and derivatives are computed along the construction of the expression. Users generally
 * don't need to call any constructors here. Creation and computing of variables are managed by the backward
 * or forward auto differentiation object. See {@link Backwarder}
 */
public class Variable {
    public String name;
    public double value;
    public Variable[] deps;
    public double[] J;
    public double v_bar;
    public double[][] H;

    /**
     * Construct an independent variable
     *
     * @param name Name of the variable
     * @param x    Value of the variable
     */
    public Variable(String name, double x) {
        this.name = name;
        this.value = x;
        this.deps = new Variable[0];
        this.J = new double[]{1.0};
        this.v_bar = 0.0;
        this.H = null;
    }

    /**
     * Construct a dependent varialble for 1st order differentiation
     *
     * @param name  Name of the variable
     * @param value Value of the variable
     * @param deps  Variables on which the one to construct depends
     * @param J     Jacobian vector
     */
    public Variable(String name, double value, Variable[] deps, double[] J) {
        this.name = name;
        this.value = value;
        this.deps = deps;
        this.J = J;
        this.v_bar = 0.0;
        this.H = null;
    }

    /**
     * Construct a dependent varialble for 2nd order differentiation
     *
     * @param name  Name of the variable
     * @param value Value of the variable
     * @param deps  Variables on which the one to construct depends
     * @param J     Jacobian vector (with respect to {@code deps}
     * @param H     Hessian matrix (with respect to {@code deps}
     */
    public Variable(String name, double value, Variable[] deps, double[] J, double[][] H) {
        this.name = name;
        this.value = value;
        this.deps = deps;
        this.J = J;
        this.v_bar = 0.0;
        this.H = H;
    }
}