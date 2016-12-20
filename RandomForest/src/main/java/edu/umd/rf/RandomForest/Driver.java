package edu.umd.rf.RandomForest;

import java.util.ArrayList;
import java.util.TreeMap;
import java.io.File;
import java.util.Scanner;
import java.io.IOException;


public class Driver{
	
	public static final int NUMTREE = 10;
	public static final int TREEDEPTH = 5;
	public static final int NUMSPLIT = 3;

    public Data readData(String filename) throws IOException{
        int numLine = 27145;
        TreeMap<Integer,Integer> labels = new TreeMap<Integer,Integer>();
        @SuppressWarnings("unchecked")
		ArrayList<String>[] dataList = new ArrayList[numLine];
        for (int i=0; i<numLine; i++){
            dataList[i] = new ArrayList<String>();
        }

        File file = new File(filename);
        Scanner fileScanner = new Scanner(file);
        while (fileScanner.hasNextLine()){
            String[] line = fileScanner.nextLine().split(",");
            int idx = labels.size();
            int lab = Integer.parseInt(line[0]);
            labels.put( idx, lab );

            for (int i = 1; i < line.length; i ++){
                String attr = line[i];
                dataList[idx].add(attr);
            }
        }
        TreeMap<Integer, String[]> data = new TreeMap<Integer,String[]>();
        for (int i=0; i < numLine; i++){
                data.put(i, Data.toStringArray(dataList[i]));
        }
        return new Data(labels, data);
    }

    public void testRF(String filename, RandomForest rf) throws IOException{
        int tp = 0, fp = 0, tn = 0, fn = 0;
        int yes = 0, total = 0;
        File file = new File(filename);
        Scanner fileScanner = new Scanner(file);
        while (fileScanner.hasNextLine()){
            String[] line = fileScanner.nextLine().split(",");

            int label = Integer.parseInt(line[0]);
            ArrayList<String> data = new ArrayList<String>();
            for (int i = 1; i < line.length; i ++){
                String attr = line[i];
                data.add(attr);
            }

            int pred = rf.predict(data);
            if (pred == 1){
                if (label == 1) tp++;
                else fp++;
            }else{
                if (label == 0) tn++;
                else fn++;
            }
            if (label == pred) yes++;
            total++;
        }
        System.out.printf("accuracy = %d / %d = %f\n", yes, total, (double)yes / total);
        System.out.printf("precision = %d / %d = %f\n", tp, tp+fp, (double)tp / (tp + fp));
        System.out.printf("recall = %d / %d = %f\n", tp, tp+fn, (double)tp / (tp + fn));
        System.out.printf("tp=%d, fp=%d, tn=%d, fn=%d\n", tp, fp, tn, fn);
        System.out.println("total : " + total);
    }

    public void run(String[] args) throws IOException{
        System.err.println("start reading data");
        Data data = readData(args[0] + "hadoop_train.csv");
        System.err.println("finish reading data, start training random forest");
        

        long startTime = System.currentTimeMillis();
        RandomForest rf = new RandomForestMR(NUMTREE, TREEDEPTH);
//        RandomForest rf = new RandomForest(NUMTREE, TREEDEPTH);
        //RandomForest rf = new RandomForest(1, 100);
        rf.train(data);
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println("Elapsed Time = " + elapsedTime / 1000.0);     
        
        System.out.println("max depth = " + TREEDEPTH);
        System.out.println("average size of tree = " + rf.size());
        
        System.err.println("finish training data, now start testing");
        testRF(args[0]+"hadoop_test.csv", rf);
    }

    public static void main(String args[]) throws IOException{

        (new Driver()).run(args);
//        (new Driver()).run(new String[]{"C:\\Users\\Cieo233\\Desktop\\RandomForest-master\\src\\main\\java\\edu\\umd\\rf\\RandomForest\\"});
    }
}
