/*
 * Copyright (c) 2012 University of Sussex
 */
package uk.ac.susx.mlcl.lib.fdist;

import java.io.Serializable;

/**
 * @author Hamish Morgan
 */
public class JointDist1 implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int M;

    private final int K;

    private final double[][] fdist;

    private final double[] xfdist;

    private final double[] yfdist;

    private final double totalFreq;

    protected JointDist1(double[][] classFeatureFreq) {
        this.fdist = classFeatureFreq;
        this.M = classFeatureFreq[0].length;
        this.K = classFeatureFreq.length;
        xfdist = calcFeatureFreqSums(classFeatureFreq);
        yfdist = calcClassFreqSums(classFeatureFreq);
        totalFreq = sum(yfdist);
    }


    /*
     * ========================================================================
     * count accessors and calculations
     * ========================================================================
     */
    public double classPresentFreq(int classId) {
        return yfdist[classId];
    }

    public double classAbsantFreq(int classId) {
        return totalFreq - yfdist[classId];
    }

    public double featurePresentFreq(int featureId) {
        return xfdist[featureId];
    }

    public double featureAbsantFreq(int featureId) {
        return totalFreq - xfdist[featureId];
    }

    public double totalFreq() {
        return totalFreq;
    }

    public double featurePresentWithClassPresentFreq(int classId, int featureId) {
        return fdist[classId][featureId];
    }

    public double featurePresentWithClassAbsentFreq(int classId, int featureId) {
        return (xfdist[featureId] - fdist[classId][featureId]);
    }

    public double featureAbsentWithClassPresentFreq(int classId, int featureId) {
        return yfdist[classId] - fdist[classId][featureId];
    }

    public double featureAbsentWithClassAbsentFreq(int classId, int featureId) {
        return totalFreq
                - yfdist[classId]
                - xfdist[featureId]
                + fdist[classId][featureId];
    }

    /*
     * ========================================================================
     * Boolean accessors and calculations
     * ========================================================================
     */
    public boolean isFeaturePresent(int featureId) {
        return xfdist[featureId] > 0;
    }

    public boolean isFeatureAbsent(int featureId) {
        return totalFreq > xfdist[featureId];
    }

    public boolean isClassPresent(int classId) {
        return yfdist[classId] > 0;
    }

    public boolean isClassAbsent(int classId) {
        return totalFreq > yfdist[classId];
    }

    public boolean isFeaturePresentWithClassPresent(int classId, int featureId) {
        return fdist[classId][featureId] > 0;
    }

    public boolean isFeaturePresentWithClassAbsent(int classId, int featureId) {
        return xfdist[featureId] > fdist[classId][featureId];
    }

    public boolean isFeatureAbsentWithClassPresent(int classId, int featureId) {
        return yfdist[classId] > fdist[classId][featureId];
    }

    public boolean isFeatureAbsentWithClassAbsent(int classId, int featureId) {
        return totalFreq > fdist[classId][featureId];
    }

    public boolean isEmpty() {
        return totalFreq() == 0;
    }

    /*
     * ========================================================================
     * Probability accessors and calculations
     * ========================================================================
     */
    public double classPresentProb(int classId) {
        return isEmpty() ? 0
               : classPresentFreq(classId) / totalFreq();
    }

    public double classAbsentProb(int classId) {
        return isEmpty() ? 0
               : classAbsantFreq(classId) / totalFreq();
    }

    public double featurePresentProb(int featureId) {
        return isEmpty() ? 0
               : featurePresentFreq(featureId) / totalFreq();
    }

    public double featureAbsentProb(int featureId) {
        return isEmpty() ? 0
               : featureAbsantFreq(featureId) / totalFreq();
    }

    public double totalProb() {
        return 1.0;
    }

    public double featurePresentWithClassPresentProb(int classId, int featureId) {
        return isEmpty() ? 0
               : featurePresentWithClassPresentFreq(classId, featureId)
                / totalFreq();
    }

    public double featurePresentWithClassAbsentProb(int classId, int featureId) {
        return isEmpty() ? 0
               : featurePresentWithClassAbsentFreq(classId, featureId)
                / totalFreq();
    }

    public double featureAbsentWithClassPresentProb(int classId, int featureId) {
        return isEmpty() ? 0
               : featureAbsentWithClassPresentFreq(classId, featureId)
                / totalFreq();
    }

    public double featureAbsentWithClassAbsentProb(int classId, int featureId) {
        return isEmpty() ? 0
               : featurePresentWithClassPresentFreq(classId, featureId)
                / totalFreq();
    }

    public double featurePresentGivenClassPresentProb(int classId, int featureId) {
        return !isClassPresent(classId) ? 0
               : featurePresentWithClassPresentFreq(classId, featureId)
                / classPresentFreq(classId);
    }

    public double featurePresentGivenClassAbsentProb(int classId, int featureId) {
        return !isClassAbsent(classId) ? 0
               : featurePresentWithClassAbsentFreq(classId, featureId)
                / classAbsantFreq(classId);
    }

    public double featureAbsentGivenClassPresentProb(int classId, int featureId) {
        return !isClassPresent(classId) ? 0
               : featureAbsentWithClassPresentFreq(classId, featureId)
                / classPresentFreq(classId);
    }

    public double featureAbsentGivenClassAbsentProb(int classId, int featureId) {
        return !isClassAbsent(classId) ? 0
               : featurePresentWithClassPresentFreq(classId, featureId)
                / classAbsantFreq(classId);
    }

    public double classPresentGivenFeaturePresentProb(int classId, int featureId) {
        return !isFeaturePresent(featureId) ? 0
               : featurePresentWithClassPresentFreq(classId, featureId)
                / featurePresentFreq(featureId);
    }

    public double classPresentGivenFeatureAbsentProb(int classId, int featureId) {
        return !isFeatureAbsent(featureId) ? 0
               : featureAbsentWithClassPresentFreq(classId, featureId)
                / featureAbsantFreq(featureId);
    }
//    // sort of the inverse posterior
//    // prob of class given any other feature
//    // sort of the inverse posterior
//    // prob of class given any other feature
//    public double classGivenFeatureAbsentProb(int classId, int featureId) {
//        return (totalFreq - featureFreq[featureId]) == 0 ? 0 
//          : (classFreq[classId] - classFeatureFreq[classId][featureId]) 
//              / (totalFreq - featureFreq[featureId]);
//    }

    public double classAbsentGivenFeaturePresentProb(int classId, int featureId) {
        return !isFeaturePresent(featureId) ? 0
               : featurePresentWithClassAbsentFreq(classId, featureId)
                / featurePresentFreq(featureId);
    }

    public double classAbsentGivenFeatureAbsentProb(int classId, int featureId) {
        return !isFeatureAbsent(featureId) ? 0
               : featureAbsentWithClassAbsentFreq(classId, featureId)
                / featureAbsantFreq(featureId);
    }

//    
//    // posterior probability of the class given the feature
//    public double classPresentGivenFeaturePresentProb(int classId, int featureId) {
//        return isFeaturePresent(featureId) ? 0
//               : f(classId, featureId) / featurePresentFreq(featureId);
//    }
//
//    public double classPresentGivenFeatureAbsentProb(int classId, int featureId) {
//        return featureFreq[featureId] == 0 ? 0
//               : jointFreq(classId, featureId) / (totalFreq - featureFreq[featureId]);
//    }
//
//    // likelyhood probability of the feature given the class
//    public double featureGivenClassProb(int classId, int featureId) {
//        return classFreq[classId] == 0 ? 0
//               : classFeatureFreq[classId][featureId] / classFreq[classId];
//    }
//
//    public double joint(int classId, int featureId) {
//        return totalFreq == 0 ? 0 : classFeatureFreq[classId][featureId] / totalFreq;
//    }
//
    /*
     * ========================================================================
     * Entropy accessors and calculations
     * ========================================================================
     */
    public double classPresentEntropy(int classId) {
        return !isClassPresent(classId) ? 0
               : entropy(classPresentProb(classId));
    }

    public double classAbsentEntropy(int classId) {
        return !isClassAbsent(classId) ? 0
               : entropy(classAbsentProb(classId));
    }

    public double classEntropy(int classId) {
        return classPresentEntropy(classId) + classAbsentEntropy(classId);
    }

    // Calculate the overall entropy of the labels, ignoring the features
    // Calculate the overall entropy of the labels, ignoring the features
    public double classBaseEntropy() {
        double baseEntropy = 0.0;
        for (int li = 0; li < K; li++) {
            baseEntropy += classPresentEntropy(li);
        }
        return baseEntropy;
    }

    public double classGivenFeaturePresentEntropy(int classId, int featureId) {
        return !isFeaturePresent(featureId) ? 0
               : entropy(classPresentGivenFeaturePresentProb(classId, featureId));
    }

    public double classGivenFeatureAbsentEntropy(int classId, int featureId) {
        return entropy(classPresentGivenFeatureAbsentProb(classId, featureId));
//        return entropy(classGivenFeatureAbsentProb(classId, featureId));
    }

    public double featurePresentEntropy(int featureId) {
        double featurePresentEntropy = 0;
        if (isFeaturePresent(featureId))
            for (int classId = 0; classId < fdist.length; classId++)
                featurePresentEntropy += classGivenFeaturePresentEntropy(classId, featureId);
        return featurePresentEntropy;
    }

    public double featureAbsentEntropy(int featureId) {
        double featureAbsentEntropy = 0;
        if (isFeatureAbsent(featureId))
            for (int classId = 0; classId < fdist.length; classId++)
                featureAbsentEntropy += classGivenFeatureAbsentEntropy(classId, featureId);
        return featureAbsentEntropy;
    }


    /*
     * ========================================================================
     * Information Gain accessors and calculations
     * ========================================================================
     */
    public double featureInfoGain(int featureId) {
        return featureInfoGain(featureId, classBaseEntropy());
    }

    public double featureInfoGain(int featureId, double classBaseEntropy) {
        return classBaseEntropy
                - featurePresentProb(featureId) * featurePresentEntropy(featureId)
                - (1 - featurePresentProb(featureId)) * featureAbsentEntropy(featureId);
    }

    public double[] featureInfoGains() {
        final double[] infogains = new double[M];
        double classBaseEntropy = classBaseEntropy();
        // Calculate the InfoGain of each feature
        // Calculate the InfoGain of each feature
        for (int featureId = 0; featureId < M; featureId++) {
            infogains[featureId] = featureInfoGain(featureId, classBaseEntropy);
        }
        return infogains;
    }

    /*
     * ========================================================================
     * Mutual Information accessors and calculations
     * ========================================================================
     */
    public double PMI(int classId, int featureId) {
        return !isFeaturePresentWithClassPresent(classId, featureId) ? 0
               : log2(classPresentGivenFeaturePresentProb(classId, featureId)
                / (classPresentProb(classId) * featurePresentProb(featureId)));
    }

    public double PosPMI(int classId, int featureId) {
        return Math.max(0, PMI(classId, featureId));
    }

    public double MI(int classId, int featureId) {
        return classPresentGivenFeaturePresentProb(classId, featureId) * PMI(classId, featureId);
    }

    public double featurePMI(int featureId) {
        double pmi = 0;
        for (int classId = 0; classId < K; classId++)
            pmi += PMI(classId, featureId);
        return pmi;
    }

    public double featurePosPMI(int featureId) {
        double posPmi = 0;
        for (int classId = 0; classId < K; classId++)
            posPmi += PosPMI(classId, featureId);
        return posPmi;
    }

    public double featureMI(int featureId) {
        double mi = 0;
        for (int classId = 0; classId < K; classId++) {
            if (isFeaturePresentWithClassPresent(classId, featureId))
                mi += featurePresentWithClassPresentProb(classId, featureId)
                        * log2(featurePresentWithClassPresentProb(classId, featureId)
                        / (classPresentProb(classId) * featurePresentProb(featureId)));
            if (isFeatureAbsentWithClassPresent(classId, featureId))
                mi += featureAbsentWithClassPresentProb(classId, featureId)
                        * log2(featureAbsentWithClassPresentProb(classId, featureId)
                        / (classPresentProb(classId) * featureAbsentProb(featureId)));
        }
//        for (int classId = 0; classId < K; classId++)
//            mi += MI(classId, featureId);
        return mi;
    }

    public double[] featureMIs() {
        final double[] mi = new double[M];
        for (int featureId = 0; featureId < M; featureId++)
            mi[featureId] = featureMI(featureId);
        return mi;
    }

    public double[] featurePMIs() {
        final double[] mi = new double[M];
        for (int featureId = 0; featureId < M; featureId++)
            mi[featureId] = featurePMI(featureId);
        return mi;
    }

    public double[] featurePosPMIs() {
        final double[] mi = new double[M];
        for (int featureId = 0; featureId < M; featureId++)
            mi[featureId] = featurePosPMI(featureId);
        return mi;
    }

    private static double[] calcFeatureFreqSums(double[][] classFeatureFreq) {
        final int K = classFeatureFreq.length;
        final int M = classFeatureFreq[0].length;
        final double[] featureFreq = new double[M];
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < K; j++) {
                featureFreq[i] += classFeatureFreq[j][i];
            }
        }
        return featureFreq;
    }

    private static double[] calcClassFreqSums(double[][] classFeatureFreq) {
        final int K = classFeatureFreq.length;
        final int M = classFeatureFreq[0].length;
        final double[] classFreq = new double[K];
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < K; j++) {
                classFreq[j] += classFeatureFreq[j][i];
            }
        }
        return classFreq;
    }

    private static double sum(double[] arr) {
        double sum = 0;
        for (int i = 0; i < arr.length; i++)
            sum += arr[i];
        return sum;
    }

    private static final double LOG2 = Math.log(2);

    private static double log2(double num) {
        return Math.log(num) / LOG2;
    }

    public static double entropy(double p) {
        return p > 0 ? -p * log2(p) : 0;
    }

}
