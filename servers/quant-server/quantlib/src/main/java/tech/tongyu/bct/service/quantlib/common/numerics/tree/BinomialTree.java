package tech.tongyu.bct.service.quantlib.common.numerics.tree;

import tech.tongyu.bct.service.quantlib.common.enums.BarrierDirection;
import tech.tongyu.bct.service.quantlib.common.enums.ExerciseType;
import tech.tongyu.bct.service.quantlib.common.enums.OptionType;
import tech.tongyu.bct.service.quantlib.common.enums.RebateType;
import tech.tongyu.bct.service.quantlib.financial.instruments.options.*;

import java.util.function.BiFunction;
import java.util.function.DoubleFunction;
import java.util.function.Function;

import static java.lang.Math.*;


/**
 * @author Liao Song
 * @since 2016-8-1
 */
public class BinomialTree {

    private double u;
    private double p;
    private double df;
    private double dt;
    private double spot;
    private double r;
    private int n;


    /**
     * Construct an instance of the binomial tree using the given parameters regardless of the type of the option.
     *
     * @param spot spot price
     * @param tau  time to maturity in years
     * @param r    risk free interest rate
     * @param q    annually dividend yield
     * @param vol  volatility
     * @param N    number of steps in constructing the tree
     */
    public BinomialTree(double spot, double tau, double r, double q, double vol, int N) {

        this.n = N;
        this.dt = tau / n;
        double B = exp(2 * (r - q) * dt + vol * vol * dt);
        double A = exp((r - q) * dt);
        this.df = exp(-r * dt);
        this.u = 0.5 * (B + 1 + sqrt((B + 1) * (B + 1) - 4 * A * A)) / A;
        double d = 1.0 / u;
        this.p = (A - d) / (u - d);
        this.spot = spot;
        this.r = r;
    }

    /**
     * This method returns the Boundary Condition function.
     * The function takes {spot, r, tau, p} as the input variables, where spot and p are the spot and option
     * price at the current node,tau is the time to maturity from the current tree node to the terminal node, r is the
     * risk free interest rate. The function gives the option value at current node of the tree with the given boundary
     * conditions,of the instrument. For instance, if s<= barrier for a down_and_out option, the function gives the
     * rebate amount at that node otherwise it gives the option value.
     *
     * @param instrument the financial instrument
     * @return returns the boundary condition of the given instrument
     * @throws Exception if the option is not supported by the binomial tree model
     */
    public static Function<double[], Double> getBoundaryConditions(Object instrument) throws Exception {
        //s,r,tau,p
        if (instrument instanceof KnockOutContinuous) {
            double barrier = ((KnockOutContinuous) instrument).getBarrier();
            double k = ((KnockOutContinuous) instrument).getRebateAmount();
            RebateType rebateType = ((KnockOutContinuous) instrument).getRebateType();
            BarrierDirection direction = ((KnockOutContinuous) instrument).getBarrierDirection();
            if (direction == BarrierDirection.DOWN_AND_OUT) {
                if (rebateType == RebateType.PAY_AT_EXPIRY)
                    return u -> u[0] <= barrier ? k * exp(-u[1] * u[2]) : u[3];
                else
                    return u -> u[0] <= barrier ? k : u[3];

            } else if (direction == BarrierDirection.UP_AND_OUT) {
                if (rebateType == RebateType.PAY_AT_EXPIRY)
                    return u -> u[0] >= barrier ? k * exp(-u[1] * u[2]) : u[3];
                else
                    return u -> u[0] >= barrier ? k : u[3];


            }
        } else if (instrument instanceof DoubleKnockOutContinuous) {
            double upperBarrier = ((DoubleKnockOutContinuous) instrument).getUpperBarrier();
            double lowerBarrier = ((DoubleKnockOutContinuous) instrument).getLowerBarrier();
            double k1 = ((DoubleKnockOutContinuous) instrument).getUpperRebate();
            double k2 = ((DoubleKnockOutContinuous) instrument).getLowerRebate();
            RebateType rebateType = ((DoubleKnockOutContinuous) instrument).getRebateType();
            if (rebateType == RebateType.PAY_AT_EXPIRY)
                return u -> (u[0] >= upperBarrier ? k1 * exp(-u[1] * u[2]) : 0 + u[0] <= lowerBarrier ? k2 * exp(-u[1] *
                        u[2]) : 0 + u[3]);
            else
                return u -> (u[0] >= upperBarrier ? k1 : 0 + u[0] <= lowerBarrier ? k2 : 0 + u[3]);

        } else if (instrument instanceof VanillaEuropean)
            return u -> u[3];
        else if (instrument instanceof VanillaAmerican)
            return u -> u[3];
        else if (instrument instanceof DigitalCash)
            return u -> u[3];

        throw new Exception("This option is not supported by Binomial Tree (boundary)");


    }

    /**
     * This method returns the exercise function of the given instrument.
     * This function takes the spot price and the adjusted option price(for example,if the s<=barrier for a down_and_out
     * option,the option price should be the rebate) as the input variables and returns max(payoff,option) for American
     * style options (immediate exercise applied) or simply returns the option price for European style options.
     *
     * @param instrument the financial instrument
     * @return returns the option price after exercise
     * @throws Exception if the option is not supported by the binomial tree model
     */
    public static BiFunction<Double, Double, Double> getExercise(Object instrument) throws Exception {
        //vanilla European
        if (instrument instanceof VanillaEuropean) {
            return (s, option) -> option;
        }
        //vanilla American
        else if (instrument instanceof VanillaAmerican) {
            if (((VanillaAmerican) instrument).getType() == OptionType.CALL)
                return (s, option) -> max(s - ((VanillaAmerican) instrument).getStrike(), option);
            else
                return (s, option) -> max(((VanillaAmerican) instrument).getStrike() - s, option);
        }
        //digital cash
        if (instrument instanceof DigitalCash) {
            return (s, option) -> option;
        }
        //knock out continuous
        else if (instrument instanceof KnockOutContinuous) {
            BarrierDirection direction = ((KnockOutContinuous) instrument).getBarrierDirection();
            double barrier = ((KnockOutContinuous) instrument).getBarrier();

            if (((KnockOutContinuous) instrument).getType() == OptionType.CALL) {
                if (((KnockOutContinuous) instrument).getExercise() == ExerciseType.EUROPEAN)
                    return (s, option) -> option;
                else if (direction == BarrierDirection.DOWN_AND_OUT)
                    return (s, option) -> (s <= barrier ? option : max(s - ((KnockOutContinuous) instrument).getStrike()
                            , option));
                else if (direction == BarrierDirection.UP_AND_OUT)
                    return (s, option) -> (s <= barrier ? option : max(s - ((KnockOutContinuous) instrument).getStrike()
                            , option));
            } else if (((KnockOutContinuous) instrument).getType() == OptionType.PUT) {
                if (((KnockOutContinuous) instrument).getExercise() == ExerciseType.EUROPEAN)
                    return (s, option) -> option;
                else if (direction == BarrierDirection.DOWN_AND_OUT)
                    return (s, option) -> (s <= barrier ? option : max(((KnockOutContinuous) instrument).getStrike()
                            - s, option));
                else if (direction == BarrierDirection.UP_AND_OUT)
                    return (s, option) -> (s <= barrier ? option : max(((KnockOutContinuous) instrument).getStrike()
                            - s, option));
            }
        }
        //double knock out continuous
        else if (instrument instanceof DoubleKnockOutContinuous) {
            double upperbarrier = ((DoubleKnockOutContinuous) instrument).getUpperBarrier();
            double lowerbarrier = ((DoubleKnockOutContinuous) instrument).getLowerBarrier();
            if (((DoubleKnockOutContinuous) instrument).getType() == OptionType.CALL) {
                if (((DoubleKnockOutContinuous) instrument).getExercise() == ExerciseType.EUROPEAN)
                    return (s, option) -> option;
                else
                    return (s, option) -> s >= upperbarrier ? option : 0 + s <= lowerbarrier ? option : 0
                            + max(s - ((DoubleKnockOutContinuous) instrument).getStrike(), option);
            } else if (((DoubleKnockOutContinuous) instrument).getType() == OptionType.PUT) {
                if (((DoubleKnockOutContinuous) instrument).getExercise() == ExerciseType.EUROPEAN)
                    return (s, option) -> option;
                else
                    return (s, option) -> s >= upperbarrier ? option : 0 + s <= lowerbarrier ? option : 0
                            + max(((DoubleKnockOutContinuous) instrument).getStrike() - s, option);
            }

        }
        throw new Exception("This option is not supported by Binomial Tree (exercise)");

    }

    /**
     * @param payoff             the payoff function of the given instrument
     * @param boundaryConditions the boundary condition of the given instrument
     * @param exercise           the exercise function of the given instrument
     * @return returns the option price of under the above conditions
     */
    public double getOptionPrice(DoubleFunction<Double> payoff, Function<double[], Double> boundaryConditions,
                                 BiFunction<Double, Double, Double> exercise) {


        double[] optionF = new double[n + 1];
        double[] optionB = new double[n + 1];


        //terminal option price
        for (int j = 0; j <= n; j++) {
            double s = getSNode(spot, n, j);
            double option = payoff.apply(s);
            double tau = 0;
            double[] srtp = new double[]{s, r, tau, option};
            optionF[j] = boundaryConditions.apply(srtp);
        }

        for (int i = n - 1; i >= 0; i--) {
            for (int j = 0; j <= i; j++) {
                double s = getSNode(spot, i, j);
                double option = (p * optionF[j] + (1.0 - p) * optionF[j + 1]) * df;
                double tau = (n - i) * dt;
                double[] srtp = new double[]{s, r, tau, option};
                optionB[j] = exercise.apply(s, boundaryConditions.apply(srtp));
                optionF[j] = optionB[j];

            }
        }

        return optionB[0];


    }

    private double getSNode(double s, int i, int j) {
        return s * pow(u, i - 2 * j);
    }


}
