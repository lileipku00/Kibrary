package io.github.kensuke1984.kibrary.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import io.github.kensuke1984.kibrary.timewindow.Timewindow;

/**
 * Utility for a function y=f(x)
 * <p>
 * <p>
 * <p>
 * <b>This class is IMMUTABLE</b>
 * </p>
 * <p>
 * TODO sorted
 *
 * @author Kensuke Konishi
 * @version 0.1.2.1
 */
public class Trace {

    /**
     * @param i index for x [0, length -1]
     * @return x[i]
     */
    public double getXAt(int i) {
        return x[i];
    }

    /**
     * @param i index for y [0, length -1]
     * @return y[i]
     */
    public double getYAt(int i) {
        return y[i];
    }

    /**
     * All lines are trimmed. Lines starting with 'c' '!' '#' are ignored.
     *
     * @param path    of the file you want to read
     * @param xColumn indicates which column is x (for the first column &rarr; 0)
     * @param yColumn indicates which column is y
     * @return Trace made by the file of the path
     * @throws IOException if any
     */
    public static Trace createTrace(Path path, int xColumn, int yColumn) throws IOException {
        List<String> lines = Files.readAllLines(path).stream().map(String::trim).filter(l -> {
            char first = l.charAt(0);
            return first != 'c' && first != '#' && first != '!';
        }).collect(Collectors.toList());
        int n = lines.size();
        double[] x = new double[n];
        double[] y = new double[n];
        for (int i = 0; i < n; i++) {
            String[] parts = lines.get(i).split("\\s+");
            x[i] = Double.parseDouble(parts[xColumn]);
            y[i] = Double.parseDouble(parts[yColumn]);
        }
        return new Trace(x, y);
    }

    /**
     * @param path of a file
     * @return Trace of x in the first column and y in the second column
     * @throws IOException if any
     */
    public static Trace createTrace(Path path) throws IOException {
        return createTrace(path, 0, 1);
    }

    private final double[] x;
    private final double[] y;
    private final RealVector xVector;
    private final RealVector yVector;

    /**
     * Deep copy
     *
     * @param x array for x
     * @param y array for y
     */
    public Trace(double[] x, double[] y) {
        if (x.length != y.length) throw new IllegalArgumentException("Input arrays have different lengths");
        this.x = x.clone();
        this.y = y.clone();
        xVector = new ArrayRealVector(x, false);
        yVector = new ArrayRealVector(y, false);
    }

    /**
     * @return the number of elements
     */
    public int getLength() {
        return x.length;
    }

    /**
     * compute n th polynomial functions for the trace
     *
     * @param n degree of polynomial
     * @return n th {@link PolynomialFunction} fitted to this
     */
    public PolynomialFunction toPolynomial(int n) {
        if (x.length <= n) throw new IllegalArgumentException("n is too big");
        if (n < 0) throw new IllegalArgumentException("n must be positive..(at least)");

        // (1,x,x**2,....)
        RealMatrix a = new Array2DRowRealMatrix(x.length, n + 1);
        for (int j = 0; j < x.length; j++)
            for (int i = 0; i <= n; i++)
                a.setEntry(j, i, Math.pow(x[j], i));
        RealMatrix at = a.transpose();
        a = at.multiply(a);
        RealVector b = at.operate(yVector);
        RealVector coef = new LUDecomposition(a).getSolver().solve(b);
        return new PolynomialFunction(coef.toArray());
    }

    /**
     * f(x) &rarr; f(x-shift) Shifts "shift" in the direction of x axis. If you
     * want to change like below: <br>
     * x:(3, 4, 5) -> (0, 1, 2) <br>
     * then the value 'shift' should be -3
     *
     * @param shift value of shift
     * @return f(x-shift), the values in y is deep copied.
     */
    public Trace shiftX(double shift) {
        return new Trace(Arrays.stream(x).map(d -> d + shift).toArray(), y);
    }

    /**
     * x=cの点でのyの値をfをn次関数として補完する 補完の際には直近のn+1点で補完
     *
     * @param n degree of function for interpolation
     * @param c point for the value
     * @return y=f(c)
     */
    public double toValue(int n, double c) {
        if (x.length < n + 1) throw new IllegalArgumentException("n is too big");
        if (n < 0) throw new IllegalArgumentException("n is invalid");

        int[] j = nearPoints(n + 1, c);

        if (n == 0) return y[j[0]];

        double[] xi = Arrays.stream(j).parallel().mapToDouble(i -> x[i]).toArray();

        // c**n + c**n-1 + .....
        RealVector cx = new ArrayRealVector(n + 1);

        RealMatrix matrix = new Array2DRowRealMatrix(n + 1, n + 1);
        // double[] b = new double[n + 1];
        RealVector bb = new ArrayRealVector(n + 1);
        for (int i = 0; i < n + 1; i++) {
            cx.setEntry(i, Math.pow(c, i));
            for (int k = 0; k < n + 1; k++)
                matrix.setEntry(i, k, Math.pow(xi[i], k));

            bb.setEntry(i, y[j[i]]);
        }

        return cx.dotProduct(new LUDecomposition(matrix).getSolver().solve(bb));
    }

    /**
     * @param target value of x to look for the nearest X value to
     * @return the closest X to the target
     */
    public double getNearestX(double target) {
        return x[getNearestXIndex(target)];
    }

    /**
     * peak is defined as 0 &lt; (y(x[i])-y(x[i-1]))*(y(x[i])-y(x[i+1]))
     *
     * @return index of a peak (ordered)
     */
    public int[] indexOfPeaks() {
        return IntStream.range(1, x.length - 1).filter(i -> 0 < (y[i + 1] - y[i]) * (y[i - 1] - y[i])).toArray();
    }

    /**
     * 0 &lt; (y(x[i])-y(x[i-1]))*(y(x[i])-y(x[i+1])) and y[i]&lt;y[i-1]
     *
     * @return index of downward convex
     */
    public int[] indexOfDownwardConvex() {
        return IntStream.range(1, x.length - 1)
                .filter(i -> y[i] < y[i - 1] && 0 < (y[i + 1] - y[i]) * (y[i - 1] - y[i])).toArray();
    }

    /**
     * 0 &lt; (y(x[i])-y(x[i-1]))*(y(x[i])-y(x[i+1])) and y[i-1] &lt; y[i]
     *
     * @return index of downward convex
     */
    public int[] indexOfUpwardConvex() {
        return IntStream.range(1, x.length - 1)
                .filter(i -> y[i - 1] < y[i] && 0 < (y[i + 1] - y[i]) * (y[i - 1] - y[i])).toArray();
    }

    /**
     * @param target value of x to look for the nearest X value to
     * @return the index of the closest X to the target
     */
    public int getNearestXIndex(double target) {
        return nearPoints(1, target)[0];
    }

    /**
     * Assume the interval of x is same as that of this.
     *
     * @param trace which length must be shorter than this.
     * @return the shift value x0 in x direction for best correlation.
     */
    public double findBestShift(Trace trace) {
        int gapLength = x.length - trace.getLength();
        if (gapLength <= 0) throw new IllegalArgumentException("Input trace must be shorter.");
        double corMax = -1;
        double compY2 = trace.yVector.getNorm();
        double shift = 0;
        for (int i = 0; i <= gapLength; i++) {
            double cor = 0;
            double y2 = 0;
            for (int j = 0; j < trace.getLength(); j++) {
                cor += y[i + j] * trace.y[j];
                y2 += y[i + j] * y[i + j];
            }
            cor /= y2 * compY2;
            if (corMax < cor) {
                shift = x[i] - trace.x[0];
                corMax = cor;
            }
        }
        return shift;
    }

    /**
     * 最も相関の高い位置を探す 探し方は、短い方をずらしていく 同じ長さだと探さない。
     *
     * @param base    array
     * @param compare array
     * @return compareを何ポイントずらすか 0だと先頭から
     */
    public static int findBestShift(double[] base, double[] compare) {
        double[] shorter;
        double[] longer;
        if (base.length == compare.length) return 0;
        if (base.length < compare.length) {
            shorter = base;
            longer = compare;
        } else {
            shorter = compare;
            longer = base;
        }
        int gap = longer.length - shorter.length;
        int bestShift = 0;
        double bestCorrelation = 0;
        for (int shift = 0; shift < gap + 1; shift++) {
            double[] partY = new double[shorter.length];
            System.arraycopy(longer, shift, partY, 0, shorter.length);
            RealVector partYVec = new ArrayRealVector(partY);
            RealVector shorterVec = new ArrayRealVector(shorter);
            double correlation = partYVec.dotProduct(shorterVec) / partYVec.getNorm() / shorterVec.getNorm();
            if (bestCorrelation < correlation) {
                bestCorrelation = correlation;
                bestShift = shift;
            }
            // System.out.println(correlation);
        }

        return compare.length < base.length ? bestShift : -bestShift;
    }

    /**
     * @param n must be a natural number
     * @param x x
     * @return xに最も近いn点の番号
     */
    private int[] nearPoints(int n, double x) {
        if (n <= 0 || this.x.length < n) throw new IllegalArgumentException("n is invalid");
        int[] xi = new int[n];
        double[] res = new double[n];
        Arrays.fill(res, -1);
        for (int i = 0; i < this.x.length; i++) {
            double residual = Math.abs(this.x[i] - x);
            for (int j = 0; j < n; j++)
                if (res[j] < 0 || residual <= res[j]) {
                    for (int k = n - 1; j < k; k--) {
                        res[k] = res[k - 1];
                        xi[k] = xi[k - 1];
                    }
                    res[j] = residual;
                    xi[j] = i;
                    break;
                }
        }
        return xi;
    }

    /**
     * thisの start &le; x &le; endの部分を切り抜く
     *
     * @param start start x of window
     * @param end   end px of window
     * @return 対象部分のtraceを返す (deep copy)
     */
    public Trace cutWindow(double start, double end) {
        List<Double> xList = new ArrayList<>();
        List<Double> yList = new ArrayList<>();
        IntStream.range(0, x.length).filter(i -> start <= x[i] && x[i] <= end).forEach(i -> {
            xList.add(x[i]);
            yList.add(y[i]);
        });
        if (xList.isEmpty()) throw new RuntimeException("No data in [" + start + ", " + end + "]");
        return new Trace(xList.stream().mapToDouble(Double::doubleValue).toArray(),
                yList.stream().mapToDouble(Double::doubleValue).toArray());
    }

    /**
     * @param timeWindow {@link Timewindow} for cut
     * @return timeWindowの内部に属する部分を切り取ったものをnewして返す
     */
    public Trace cutWindow(Timewindow timeWindow) {
        return cutWindow(timeWindow.getStartTime(), timeWindow.getEndTime());
    }

    /**
     * @return DEEP copy of x
     */
    public double[] getX() {
        return x.clone();
    }

    /**
     * @return DEEP copy of y
     */
    public double[] getY() {
        return y.clone();
    }

    /**
     * @return x which gives maximum y
     */
    public double getXforMaxValue() {
        return x[yVector.getMaxIndex()];
    }

    /**
     * @return x which gives minimum y
     */
    public double getXforMinValue() {
        return x[yVector.getMinIndex()];
    }

    /**
     * @return maximum value of y
     */
    public double getMaxValue() {
        return yVector.getMaxValue();
    }

    /**
     * @return minimum value of y
     */
    public double getMinValue() {
        return yVector.getMinValue();
    }

    /**
     * @return (deep) copy of X
     */
    public RealVector getXVector() {
        return xVector.copy();
    }

    /**
     * @return (deep) copy of Y
     */
    public RealVector getYVector() {
        return yVector.copy();
    }

    /**
     * x in this and trace must be same. i.e. all the x elements must be same
     *
     * @param trace to be added
     * @return new Trace after the addition
     */
    public Trace add(Trace trace) {
        if (!Arrays.equals(x, trace.x)) throw new IllegalArgumentException("Trace to be added has different x axis.");
        return new Trace(x, yVector.add(trace.yVector).toArray());
    }

    /**
     * @param d to be multiplied
     * @return Trace which Y is multiplied d
     */
    public Trace multiply(double d) {
        return new Trace(x, yVector.mapMultiply(d).toArray());
    }

    /**
     * @return the average value of y
     */
    public double average() {
        return Arrays.stream(y).average().getAsDouble();
    }

    /**
     * 1/n&times;&Sigma;(y<sub>i</sub> - ymean)<sup>2</sup>
     *
     * @return standard deviation of y
     */
    public double standardDeviation() {
        double average = average();
        return Arrays.stream(y).map(d -> d - average).map(d -> d * d).sum() / y.length;
    }

}
