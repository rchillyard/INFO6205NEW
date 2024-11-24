package edu.neu.coe.info6205.functions;

import java.util.function.DoubleFunction;

/**
 * This class models the Newton-Raphson Approximation algorithm.
 * See <a href="https://en.wikipedia.org/wiki/Newton%27s_method">...</a>
 * It is an example of a non-deterministic algorithm inasmuch as the convergence (or lack thereof) is very dependent
 * on the value of the initial guess x0 to the solve method.
 * However, if you run it with identical starting conditions, it will always come out the same: it does not use any random elements.
 */
public class Newton {

    /**
     * Public constructor to create a problem to be solved by Newton's method.
     * In particular, the problem is to find a root of the equation f(x) = 0.
     *
     * @param equation a String rendition of the function in the form f(x).
     *                 There is no need to add the "= 0" part of the equation.
     * @param f        the function f(x)
     * @param dfbydx   the first derivative of f(x) with respect to the variable x
     */
    public Newton(final String equation, final DoubleFunction<Double> f, final DoubleFunction<Double> dfbydx) {
        this.equation = equation;
        this.f = f;
        this.dfbydx = dfbydx;
    }

    /**
     * Method to solve this Newton problem.
     *
     * @param x0        the initial estimate of x.
     *                  If this is too far from any root, the solution may not converge.
     * @param maxTries  the maximum number of tries before admitting defeat due to non-convergence.
     * @param tolerance the required precision for the value of f(x) to be considered equal to zero.
     * @return either a Double (the actual root) or a String explaining why no root could be found.
     */
    public Either<String, Double> solve(final double x0, final int maxTries, final double tolerance) {
        double x = x0;
        int tries = maxTries;
        for (; tries > 0; tries--)
            try {
                final double y = f.apply(x);
                if (Math.abs(y) < tolerance) return Either.right(x);
                x = x - y / dfbydx.apply(x);
            } catch (Exception e) {
                return Either.left("Exception thrown solving " + equation + "=0, given x0=" + x0 + ", maxTries=" + maxTries + ", and tolerance=" + tolerance + " because " + e.getLocalizedMessage());
            }
        return Either.left(equation + "=0 did not converge given x0=" + x0 + ", maxTries=" + maxTries + ", and tolerance=" + tolerance);
    }

    public static void main(String[] args) {

        // Build the Newton's Approximation problem to be solved: 3x^4-6
        Newton newton = new Newton("3x^4-6", (double x) -> 3*x*x*x*x-6, (double x) -> 12 * x * x * x );

        // Solve the problem starting with a value of x = 1;
        // requiring a precision of 10^-7;
        // and giving up after 200 tries.
        Either<String, Double> result = newton.solve(1.0, 200, 1E-7);

        // Process the result
        result.apply(
                // Admit defeat, explaining why on syserr...
                System.err::println,
                aDouble -> {
                    // Publish the happy news.
                    System.out.println("Good news! " + newton.equation + " was solved: " + aDouble);
                });
    }

    private final String equation;
    private final DoubleFunction<Double> f;
    private final DoubleFunction<Double> dfbydx;
}