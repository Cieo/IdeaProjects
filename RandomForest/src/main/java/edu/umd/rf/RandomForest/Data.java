package edu.umd.rf.RandomForest;

import org.apache.commons.lang.math.Range;

import java.awt.font.NumericShaper;
import java.io.Serializable;
import java.util.*;

public class Data implements Serializable {

    private static final long serialVersionUID = 1779688994223706632L;

    protected int numExamples;
    protected int numPositive;

    protected TreeMap<Integer, String[]> data;     // map (example id) -> (list of feature)
    protected TreeMap<Integer, Integer> labels; // map (example id) -> (0/1 label)

    public Data(TreeMap<Integer, Integer> labels, TreeMap<Integer, String[]> data) {
        this.data = data;
        this.labels = labels;
        this.numExamples = labels.size();
        for (Integer label : labels.values())
            numPositive += (label == 1) ? 1 : 0;
    }

    public TreeMap<Integer, String[]> getData() {
        return data;
    }

    public TreeMap<Integer, Integer> getLabels() {
        return labels;
    }

    public int getMajority() {
        int[] c = {0, 0};
        for (Integer label : labels.values()) {
            c[label]++;
        }
        return (c[0] > c[1]) ? 0 : 1;
    }

    public int[] featureRandomSubset(int size) {
        Random rand = new Random();
        int[] features = range(0, data.get(data.firstKey()).length);
        int[] a = new int[size];
        for (int i = 0; i < size; i++) {
            a[i] = features[rand.nextInt(features.length)];
        }
        return a;
    }

    public int[] range(int start, int finish) {
        int[] a = new int[finish - start];
        for (int i = start; i < finish; i++) {
            a[i - start] = i;
        }
        return a;
    }

    @SuppressWarnings("unused")
    private double entropy(double x, double y) {
        double p = x / y;
        return -(p * Math.log(p) + (1 - p) * Math.log(1 - p));
    }

    private double gini(double positive, double total) {
        double negative = total - positive;
        return 1 - (positive / total) * (positive / total) - (negative / total) * (negative / total);
    }


    // TODO information gain
    // Gini index used now
    public double computeScore(int feat) {
        TreeMap<String, Integer> countFeatures = new TreeMap<String, Integer>();
        TreeMap<String, Integer> countLabels = new TreeMap<String, Integer>();
        String feature;
        for (int i : data.keySet()) {
            feature = data.get(i)[feat];

            if (countFeatures.containsKey(feature)) {
                countFeatures.put(feature, countFeatures.get(feature) + 1);
                countLabels.put(feature, countLabels.get(feature) + labels.get(i));
            } else {
                countFeatures.put(feature, 1);
                countLabels.put(feature, labels.get(i));
            }
        }
        double g = 0;
        for (String f : countFeatures.keySet()) {
            g += ((countFeatures.get(f) + 0.0) / numExamples) * gini(countLabels.get(f), countFeatures.get(f));
        }
        return g;
    }

    public DataPair split(int feat) {
        int[] examples = range(0, numExamples);
        if (examples.length == 0)
            return null;
        List<Integer> index = new ArrayList<Integer>();
        for (int i : data.keySet()) {
            if (data.get(i)[feat].equals("1")) {
                index.add(i);
            }
        }


        TreeMap<Integer, Integer> leftLabels = new TreeMap<Integer, Integer>();
        TreeMap<Integer, Integer> rightLabels = new TreeMap<Integer, Integer>();
        for (Map.Entry<Integer, Integer> entry : labels.entrySet()) {
            if (index.contains(entry.getKey())) {
                leftLabels.put(entry.getKey(), entry.getValue());
            } else {
                rightLabels.put(entry.getKey(), entry.getValue());
            }
        }

        TreeMap<Integer, String[]> leftMap = new TreeMap<Integer, String[]>();
        TreeMap<Integer, String[]> rightMap = new TreeMap<Integer, String[]>();
        for (Map.Entry<Integer, String[]> entry : data.entrySet()) {
            if (index.contains(entry.getKey())) {
                leftMap.put(entry.getKey(), entry.getValue());
            } else {
                rightMap.put(entry.getKey(), entry.getValue());
            }
        }

        Data leftData = new Data(leftLabels, leftMap);
        Data rightData = new Data(rightLabels, rightMap);
        return new DataPair(leftData, rightData);
    }

    public static int[] toIntArray(ArrayList<Integer> list) {
        int[] a = new int[list.size()];
        for (int i = 0; i < a.length; i++) {
            a[i] = list.get(i);
        }
        return a;
    }

    public static String[] toStringArray(ArrayList<String> list) {
        String[] a = new String[list.size()];
        for (int i = 0; i < a.length; i++) {
            a[i] = list.get(i);
        }
        return a;
    }

    public boolean monotone() {
        int x = 0;
        for (Integer label : labels.values())
            x += label;
        return x == 0 || x == labels.size();
    }

    public int getNumExamples() {
        return this.numExamples;
    }
}
