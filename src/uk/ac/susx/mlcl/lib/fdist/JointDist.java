/*
 * Copyright (c) 2012 University of Sussex
 */
package uk.ac.susx.mlcl.lib.fdist;

import java.io.Serializable;

/**
 * @author Hamish Morgan
 */
public class JointDist implements Serializable, Dist2 {

    private static final long serialVersionUID = 1L;

    private final double[][] freqs;

    private final EmpDist xTotals;

    private final EmpDist yTotals;

    private final double totalFreq;

    protected JointDist(double[][] classFeatureFreq) {
        this.freqs = classFeatureFreq;
        xTotals = calcXDist(classFeatureFreq);
        yTotals = calcYDist(classFeatureFreq);
        assert xTotals.getTotalFreq() == yTotals.getTotalFreq();
        totalFreq = xTotals.getTotalFreq();
    }

    @Override
    public EmpDist getXTotalsDist() {
        return xTotals;
    }

    @Override
    public EmpDist getYTotalsDist() {
        return yTotals;
    }

    public Dist1 getXWithYPresentDist(final int y) {
        return new AbstractDist1() {

            @Override
            public double getPresentFreq(int x) {
                return freqs[x][y];
            }

            @Override
            public double getTotalFreq() {
                return yTotals.getPresentFreq(y);
            }

        };
    }

    public Dist1 getXWithYAbsentDist(final int y) {
        return new AbstractDist1() {

            @Override
            public double getPresentFreq(int x) {
                return xTotals.getPresentFreq(x) - freqs[x][y];
            }

            @Override
            public double getTotalFreq() {
                return getTotalFreq() - yTotals.getPresentFreq(y);
            }

        };
    }

    public Dist1 getYWithXPresentDist(final int x) {
        return new AbstractDist1() {

            @Override
            public double getPresentFreq(int y) {
                return freqs[x][y];
            }

            @Override
            public double getTotalFreq() {
                return xTotals.getPresentFreq(x);
            }

        };
    }


    public Dist1 getYWithXAbsentDist(final int x) {
        return new AbstractDist1() {

            @Override
            public double getPresentFreq(int y) {
                return yTotals.getPresentFreq(y) - freqs[x][y];
            }

            @Override
            public double getTotalFreq() {
                return getTotalFreq() - xTotals.getPresentFreq(x);
            }
        };
    }

    /*
     * ========================================================================
     * count accessors and calculations
     * ========================================================================
     */
    @Override
    public double getTotalFreq() {
        return totalFreq;
    }

    @Override
    public double getXPresentWithYPresentFreq(int x, int y) {
        return freqs[x][y];
    }

    @Override
    public double getXPresentWithYAbsentFreq(int x, int y) {
        return xTotals.getPresentFreq(x) - getXPresentWithYPresentFreq(x, y);
    }

    @Override
    public double getXAbsentWithYPresentFreq(int x, int y) {
        return yTotals.getPresentFreq(y) - getXPresentWithYPresentFreq(x, y);
    }

    @Override
    public double getXAbsentWithYAbsentFreq(int x, int y) {
        return getTotalFreq()
                - xTotals.getPresentFreq(x)
                - yTotals.getPresentFreq(y)
                + getXPresentWithYPresentFreq(x, y);
    }

    /*
     * ========================================================================
     * Boolean accessors and calculations
     * ========================================================================
     */
    @Override
    public boolean isXPresentWithYPresent(int x, int y) {
        return freqs[x][y] > 0;
    }

    @Override
    public boolean isXPresentWithYAbsent(int x, int y) {
        return xTotals.getPresentFreq(x) > getXPresentWithYPresentFreq(x, y);
    }

    @Override
    public boolean isXAbsentWithYPresent(int x, int y) {
        return yTotals.getPresentFreq(y) > getXPresentWithYPresentFreq(x, y);
    }

    @Override
    public boolean isXAbsentWithYAbsent(int x, int y) {
        return getTotalFreq() > getXPresentWithYPresentFreq(x, y);
    }

    public boolean isEmpty() {
        return getTotalFreq() == 0;
    }

    /*
     * ========================================================================
     * Probability accessors and calculations
     * ========================================================================
     */
    @Override
    public double getXPresentWithYPresentProb(int x, int y) {
        return isEmpty() ? 0
               : getXPresentWithYPresentFreq(x, y)
                / getTotalFreq();
    }

    @Override
    public double getXPresentWithYAbsentProb(int x, int y) {
        return isEmpty() ? 0
               : getXPresentWithYAbsentFreq(x, y)
                / getTotalFreq();
    }

    @Override
    public double getXAbsentWithYPresentProb(int x, int y) {
        return isEmpty() ? 0
               : getXAbsentWithYPresentFreq(x, y)
                / getTotalFreq();
    }

    @Override
    public double getXAbsentWithYAbsentProb(int x, int y) {
        return isEmpty() ? 0
               : getXPresentWithYPresentFreq(x, y)
                / getTotalFreq();
    }
    /*
     * ========================================================================
     * xxx
     * ========================================================================
     */

    public Dist1 getXGivenYPresentDist(final int y) {
        return new AbstractDist1() {

            @Override
            public double getPresentFreq(int x) {
                return getXPresentWithYPresentFreq(x, y);
            }

            @Override
            public double getTotalFreq() {
                return getYTotalsDist().getPresentFreq(y);
            }

        };
    }

    public Dist1 getXGivenYAbsentDist(final int y) {
        return new AbstractDist1() {

            @Override
            public double getPresentFreq(int x) {
                return getXPresentWithYAbsentFreq(x, y);
            }

            @Override
            public double getTotalFreq() {
                return getYTotalsDist().getAbsentFreq(y);
            }

        };
    }

    public Dist1 getYGivenXPresentDist(final int x) {
        return new AbstractDist1() {

            @Override
            public double getPresentFreq(int y) {
                return getXPresentWithYPresentFreq(x, y);
            }

            @Override
            public double getTotalFreq() {
                return getXTotalsDist().getPresentFreq(x);
            }

        };
    }

    public Dist1 getYGivenXAbsentDist(final int x) {
        return new AbstractDist1() {

            @Override
            public double getPresentFreq(int y) {
                return getXAbsentWithYPresentFreq(x, y);
            }

            @Override
            public double getTotalFreq() {
                return getXTotalsDist().getPresentFreq(x);
            }

        };
    }
//
//    public double featurePresentGivenClassPresentProb(int y, int x) {
//        return !isClassPresent(y) ? 0
//               : getXPresentWithYPresentFreq(y, x)
//                / classPresentFreq(y);
//    }
//
//    public double featurePresentGivenClassAbsentProb(int classId, int x) {
//        return !isClassAbsent(classId) ? 0
//               : getXPresentWithYAbsentFreq(classId, x)
//                / classAbsantFreq(classId);
//    }
//
//    public double featureAbsentGivenClassPresentProb(int classId, int featureId) {
//        return !isClassPresent(classId) ? 0
//               : getXAbsentWithYPresentFreq(classId, featureId)
//                / classPresentFreq(classId);
//    }
//
//    public double featureAbsentGivenClassAbsentProb(int classId, int featureId) {
//        return !isClassAbsent(classId) ? 0
//               : getXPresentWithYPresentFreq(classId, featureId)
//                / classAbsantFreq(classId);
//    }
//
//    public double classPresentGivenFeaturePresentProb(int classId, int featureId) {
//        return !isFeaturePresent(featureId) ? 0
//               : getXPresentWithYPresentFreq(classId, featureId)
//                / featurePresentFreq(featureId);
//    }
//
//    public double classPresentGivenFeatureAbsentProb(int classId, int featureId) {
//        return !isFeatureAbsent(featureId) ? 0
//               : getXAbsentWithYPresentFreq(classId, featureId)
//                / featureAbsantFreq(featureId);
//    }
//    public double classAbsentGivenFeaturePresentProb(int classId, int featureId) {
//        return !isFeaturePresent(featureId) ? 0
//               : getXPresentWithYAbsentFreq(classId, featureId)
//                / featurePresentFreq(featureId);
//    }
//
//    public double classAbsentGivenFeatureAbsentProb(int classId, int featureId) {
//        return !isFeatureAbsent(featureId) ? 0
//               : getXAbsentWithYAbsentFreq(classId, featureId)
//                / featureAbsantFreq(featureId);
//    }
//   
    /*
     * ========================================================================
     * Entropy accessors and calculations
     * ========================================================================
     */
//    public double classPresentEntropy(int classId) {
//        return !isClassPresent(classId) ? 0
//               : entropy(classPresentProb(classId));
//    }
//
//    public double classAbsentEntropy(int classId) {
//        return !isClassAbsent(classId) ? 0
//               : entropy(classAbsentProb(classId));
//    }
//
//    public double classEntropy(int classId) {
//        return classPresentEntropy(classId) + classAbsentEntropy(classId);
//    }
//
//    // Calculate the overall entropy of the labels, ignoring the features
//    // Calculate the overall entropy of the labels, ignoring the features
//    public double classBaseEntropy() {
//        double baseEntropy = 0.0;
//        for (int li = 0; li < N; li++) {
//            baseEntropy += classPresentEntropy(li);
//        }
//        return baseEntropy;
//    }
//
//    public double classGivenFeaturePresentEntropy(int classId, int featureId) {
//        return !isFeaturePresent(featureId) ? 0
//               : entropy(classPresentGivenFeaturePresentProb(classId, featureId));
//    }
//
//    public double classGivenFeatureAbsentEntropy(int classId, int featureId) {
//        return entropy(classPresentGivenFeatureAbsentProb(classId, featureId));
////        return entropy(classGivenFeatureAbsentProb(classId, featureId));
//    }
//
//    public double featurePresentEntropy(int featureId) {
//        double featurePresentEntropy = 0;
//        if (isFeaturePresent(featureId))
//            for (int classId = 0; classId < freqs.length; classId++)
//                featurePresentEntropy += classGivenFeaturePresentEntropy(classId, featureId);
//        return featurePresentEntropy;
//    }
//
//    public double featureAbsentEntropy(int featureId) {
//        double featureAbsentEntropy = 0;
//        if (isFeatureAbsent(featureId))
//            for (int classId = 0; classId < freqs.length; classId++)
//                featureAbsentEntropy += classGivenFeatureAbsentEntropy(classId, featureId);
//        return featureAbsentEntropy;
//    }
//
//
//    /*
//     * ========================================================================
//     * Information Gain accessors and calculations
//     * ========================================================================
//     */
//    public double featureInfoGain(int featureId) {
//        return featureInfoGain(featureId, classBaseEntropy());
//    }
//
//    public double featureInfoGain(int featureId, double classBaseEntropy) {
//        return classBaseEntropy
//                - featurePresentProb(featureId) * featurePresentEntropy(featureId)
//                - (1 - featurePresentProb(featureId)) * featureAbsentEntropy(featureId);
//    }
//
//    public double[] featureInfoGains() {
//        final double[] infogains = new double[M];
//        double classBaseEntropy = classBaseEntropy();
//        // Calculate the InfoGain of each feature
//        // Calculate the InfoGain of each feature
//        for (int featureId = 0; featureId < M; featureId++) {
//            infogains[featureId] = featureInfoGain(featureId, classBaseEntropy);
//        }
//        return infogains;
//    }
//
//    /*
//     * ========================================================================
//     * Mutual Information accessors and calculations
//     * ========================================================================
//     */
//    public double PMI(int classId, int featureId) {
//        return !isXPresentWithYPresent(classId, featureId) ? 0
//               : log2(classPresentGivenFeaturePresentProb(classId, featureId)
//                / (classPresentProb(classId) * featurePresentProb(featureId)));
//    }
//
//    public double PosPMI(int classId, int featureId) {
//        return Math.max(0, PMI(classId, featureId));
//    }
//
//    public double MI(int classId, int featureId) {
//        return classPresentGivenFeaturePresentProb(classId, featureId) * PMI(classId, featureId);
//    }
//
//    public double featurePMI(int featureId) {
//        double pmi = 0;
//        for (int classId = 0; classId < N; classId++)
//            pmi += PMI(classId, featureId);
//        return pmi;
//    }
//
//    public double featurePosPMI(int featureId) {
//        double posPmi = 0;
//        for (int classId = 0; classId < N; classId++)
//            posPmi += PosPMI(classId, featureId);
//        return posPmi;
//    }
//
//    public double featureMI(int featureId) {
//        double mi = 0;
//        for (int classId = 0; classId < N; classId++) {
//            if (isXPresentWithYPresent(classId, featureId))
//                mi += getXPresentWithYPresentProb(classId, featureId)
//                        * log2(getXPresentWithYPresentProb(classId, featureId)
//                        / (classPresentProb(classId) * featurePresentProb(featureId)));
//            if (isXAbsentWithYPresent(classId, featureId))
//                mi += getXAbsentWithYPresentProb(classId, featureId)
//                        * log2(getXAbsentWithYPresentProb(classId, featureId)
//                        / (classPresentProb(classId) * featureAbsentProb(featureId)));
//        }
////        for (int classId = 0; classId < K; classId++)
////            mi += MI(classId, featureId);
//        return mi;
//    }
//
//    public double[] featureMIs() {
//        final double[] mi = new double[M];
//        for (int featureId = 0; featureId < M; featureId++)
//            mi[featureId] = featureMI(featureId);
//        return mi;
//    }
//
//    public double[] featurePMIs() {
//        final double[] mi = new double[M];
//        for (int featureId = 0; featureId < M; featureId++)
//            mi[featureId] = featurePMI(featureId);
//        return mi;
//    }
//
//    public double[] featurePosPMIs() {
//        final double[] mi = new double[M];
//        for (int featureId = 0; featureId < M; featureId++)
//            mi[featureId] = featurePosPMI(featureId);
//        return mi;
//    }

    private static EmpDist calcXDist(double[][] freqs) {
        final int N = freqs.length;
        final int M = freqs[0].length;
        final double[] xFreqs = new double[M];
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                xFreqs[i] += freqs[j][i];
            }
        }
        return new EmpDist(xFreqs);
    }

    private static EmpDist calcYDist(double[][] freqs) {
        final int N = freqs.length;
        final int M = freqs[0].length;
        final double[] yFreqs = new double[N];
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                yFreqs[j] += freqs[j][i];
            }
        }
        return new EmpDist(yFreqs);
    }
//
//    private static double sum(double[] arr) {
//        double sum = 0;
//        for (int i = 0; i < arr.length; i++)
//            sum += arr[i];
//        return sum;
//    }

//    private static final double LOG2 = Math.log(2);
//
//    private static double log2(double num) {
//        return Math.log(num) / LOG2;
//    }
//
//    public static double entropy(double p) {
//        return p > 0 ? -p * log2(p) : 0;
//    }
}
