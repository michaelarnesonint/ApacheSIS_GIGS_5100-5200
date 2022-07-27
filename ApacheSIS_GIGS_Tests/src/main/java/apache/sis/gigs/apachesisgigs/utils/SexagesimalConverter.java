package apache.sis.gigs.apachesisgigs.utils;

import static java.lang.Double.doubleToRawLongBits;

public class SexagesimalConverter {

    /**
     * The maximal exponent value such as {@code parseDouble("1E+308")} still a
     * finite number.
     *
     * @see Double#MAX_VALUE
     */
    static final int EXPONENT_FOR_MAX = 308;

    /**
     * The greatest power of 10 such as
     * {@code Math.pow(10, EXPONENT_FOR_ZERO) == 0}. This is the exponent in
     * {@code parseDouble("1E-324")} &lt; {@link Double#MIN_VALUE}, which is
     * stored as zero because non-representable as a {@code double} value. The
     * next power, {@code parseDouble("1E-323")}, is a non-zero {@code double}
     * value.
     *
     * @see Double#MIN_VALUE
     */
    static final int EXPONENT_FOR_ZERO = -324;

    /**
     * Table of integer powers of 10, precomputed both for performance and
     * accuracy reasons. This table consumes 4.9 kb of memory. We pay this cost
     * because integer powers of ten are requested often, and
     * {@link Math#pow(double, double)} has slight rounding errors.
     *
     * @see #pow10(int)
     */
    private static final double[] POW10 = new double[EXPONENT_FOR_MAX - EXPONENT_FOR_ZERO];

    static {
        final StringBuilder buffer = new StringBuilder("1E");
        for (int i = 0; i < POW10.length; i++) {
            buffer.setLength(2);
            buffer.append(i + (EXPONENT_FOR_ZERO + 1));
            /*
             * Double.parseDouble("1E"+i) gives as good or better numbers than Math.pow(10,i)
             * for ALL integer powers, but is slower. We hope that the current workaround is only
             * temporary. See http://developer.java.sun.com/developer/bugParade/bugs/4358794.html
             */
            POW10[i] = Double.parseDouble(buffer.toString());
        }
    }

    /**
     * Bit mask to isolate the sign bit of
     * non-{@linkplain Double#isNaN(double) NaN} values in a {@code double}. For
     * any real value, the following code evaluate to 0 if the given value is
     * positive:
     *
     * {
     *
     * @preformat java Double.doubleToRawLongBits(value) & SIGN_BIT_MASK; }
     *
     * Note that this idiom differentiates positive zero from negative zero. It
     * should be used only when such difference matter.
     */
    public static final long SIGN_BIT_MASK = Long.MIN_VALUE;

    /**
     * Number of bits in the significand (mantissa) part of IEEE 754
     * {@code double} representation,
     * <strong>not</strong> including the hidden bit.
     */
    public static final int SIGNIFICAND_SIZE = 52;
    private static final double EPS = 1E-10;
    private final double divider;
    private final boolean hasSeconds;

    public SexagesimalConverter() {
        this(true, 10000.0);
    }

    public SexagesimalConverter(final boolean hasSeconds, final double divider) {
        this.hasSeconds = hasSeconds;
        this.divider = divider;
    }

    /**
     * Performs a conversion from sexagesimal degrees to fractional degrees.
     *
     * @throws IllegalArgumentException If the given angle can not be converted.
     */
    public double convertSexagesimalToDecimalDegrees(final double angle) throws IllegalArgumentException {
        double deg, min, sec, mgn;
        if (hasSeconds) {
            sec = mgn = angle * divider;
            deg = truncate(sec / 10000);
            sec -= 10000 * deg;
            min = truncate(sec / 100);
            sec -= 100 * min;
            sec = fixRoundingError(sec, mgn);
        } else {
            sec = 0;
            min = mgn = angle * divider;
            deg = truncate(min / 100);
            min -= deg * 100;
            min = fixRoundingError(min, mgn);
        }
        if (min <= -60 || min >= 60) {                              // Do not enter for NaN
            if (Math.abs(Math.abs(min) - 100) <= (EPS * 100)) {
                if (min >= 0) {
                    deg++;
                } else {
                    deg--;
                }
                min = 0;
            } else {
                throw new IllegalArgumentException("Invalid minute value");
            }
        }
        if (sec <= -60 || sec >= 60) {                              // Do not enter for NaN
            if (Math.abs(Math.abs(sec) - 100) <= (EPS * 100)) {
                if (sec >= 0) {
                    min++;
                } else {
                    min--;
                }
                sec = 0;
            } else {
                throw new IllegalArgumentException("Invalid seconds value");
            }
        }
        return (sec / 60 + min) / 60 + deg;
    }

    /**
     * After calculation of the remaining seconds or minutes, trims the rounding
     * errors presumably caused by rounding errors in floating point arithmetic.
     * This is required for avoiding the following conversion issue:
     *
     * <ol>
     * <li>Sexagesimal value: 46.570866 (from 46°57'8.66"N in EPSG:2056
     * projected CRS)</li>
     * <li>value * 10000 = 465708.66000000003</li>
     * <li>deg = 46, min = 57, deg = 8.660000000032596</li>
     * </ol>
     *
     * We perform a rounding based on the representation in base 10 because
     * extractions of degrees and minutes fields from the sexagesimal value
     * themselves use arithmetic in base 10. This conversion is used in contexts
     * where the sexagesimal value, as shown in a number in base 10, is
     * definitive.
     *
     * @param remainder the value to fix, after other fields (degrees and/or
     * minutes) have been subtracted.
     * @param magnitude value of {@code remainder} before the degrees and/or
     * minutes were subtracted.
     */
    private static double fixRoundingError(double remainder, final double magnitude) {
        /*
             * We use 1 ULP because the double value parsed from a string representation was at 0.5 ULP
             * from the real value, and the multiplication by 'divider' add another 0.5 ULP rounding error.
             * Removal of degrees and/or minutes fields as integers do not add rounding errors.
         */
        int p = Math.getExponent(Math.ulp(magnitude));          // Power of 2 (negative for fractional value).
        if (p < 0 && p >= -SIGNIFICAND_SIZE) {         // Precision is a fraction digit >= Math.ulp(1).
            p = toExp10(-p);                           // Positive power of 10, rounded to lower value.
            final double scale = pow10(p);
            remainder = Math.rint(remainder * scale) / scale;
        }
        return remainder;
    }

    /**
     * Truncates the given value toward zero. Invoking this method is equivalent
     * to invoking {@link Math#floor(double)} if the value is positive, or
     * {@link Math#ceil(double)} if the value is negative.
     *
     * @param value the value to truncate.
     * @return the largest in magnitude (further from zero) integer value which
     * is equals or less in magnitude than the given value.
     */
    private static double truncate(final double value) {
        return (doubleToRawLongBits(value) & SIGN_BIT_MASK) == 0 ? Math.floor(value) : Math.ceil(value);
    }

    /**
     * Converts a power of 2 to a power of 10, rounded toward negative infinity.
     * This method is equivalent to the following code, but using only integer
     * arithmetic:
     *
     * {
     *
     * @preformat java return (int) Math.floor(exp2 * LOG10_2); }
     *
     * This method is valid only for arguments in the [-2620 … 2620] range,
     * which is more than enough for the range of {@code double} exponents. We
     * do not put this method in public API because it does not check the
     * argument validity.
     *
     * <h4>Arithmetic notes</h4> {@code toExp10(getExponent(10?))} returns
     * <var>n</var> only for {@code n == 0}, and <var>n</var>-1 in all other
     * cases. This is because 10? == m × 2<sup>exp2</sup> where the <var>m</var>
     * significand is always greater than 1, which must be compensated by a
     * smaller {@code exp2} value such as {@code toExp10(exp2) < n}. Note that
     * if the {@code getExponent(…)} argument is not a power of 10, then the
     * result can be either <var>n</var> or <var>n</var>-1.
     *
     * @param exp2 the power of 2 to convert Must be in the [-2620 … 2620]
     * range.
     * @return the power of 10, rounded toward negative infinity.
     */
    private static int toExp10(final int exp2) {
        /*
         * Compute:
         *          exp2 × (log10(2) × 2?) / 2?
         * where:
         *          n = 20   (arbitrary value)
         *
         * log10(2) × 2?  =  315652.82873335475, which we round to 315653.
         *
         * The range of valid values for such approximation is determined
         * empirically by running the NumericsTest.testToExp10() method.
         */
        assert exp2 >= -2620 && exp2 <= 2620 : exp2;
        return (exp2 * 315653) >> 20;
    }

    /**
     * Computes 10 raised to the power of <var>x</var>. This is the
     * implementation of the public {@link MathFunctions#pow10(int)} method,
     * defined here in order to allow the JVM to initialize the {@link #POW10}
     * table only when first needed.
     *
     * @param x the exponent.
     * @return 10 raised to the given exponent.
     */
    private static double pow10(int x) {
        x -= EXPONENT_FOR_ZERO + 1;
        return (x >= 0) ? (x < POW10.length ? POW10[x] : Double.POSITIVE_INFINITY) : 0;
    }

}
