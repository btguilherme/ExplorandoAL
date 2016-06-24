/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package results;

import io.IOText;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author guilherme
 */
public class Resultados {

    public static void main(String[] args) {
        System.err.println("opf super");
        mediaDesvioPadrao("opfsuper");
        
        System.err.println("svm cross");
        mediaDesvioPadrao("svmcross");
        
        System.err.println("opf semi");
        mediaDesvioPadrao("opfsemi");
    }

    public static void mediaDesvioPadrao(String classifierType) {

        String folderResults = null;
        String trainingFileName = null;
        boolean svmBased = false;
        boolean opfBased = false;

        switch (classifierType) {
            case "opfsuper":
                folderResults = "opf_results";
                trainingFileName = "training.time";
                opfBased = true;
                break;
            case "opfsemi":
                folderResults = "opfsemi_results";
                trainingFileName = "Z1LINE.time";
                opfBased = true;
                break;
            case "svmgrid":
                folderResults = "grid_results";
                svmBased = true;
                break;
            case "svmcross":
                folderResults = "svm_results";
                svmBased = true;
                break;
        }

        IOText io = new IOText();

        String execution = "execution_";
        String prePath = System.getProperty("user.dir").concat(File.separator);
        File diretorioExec;

        int indiceExec = 0;
        int indiceIt = 0;

        List<Double> sumAccs = new ArrayList<>();
        List<Double> sumt_tests = new ArrayList<>();
        List<Double> sumt_trains = new ArrayList<>();
        List<Double> sumt_selecs = new ArrayList<>();

        List<List<Double>> todosValoresAcc = new ArrayList<>();
        List<List<Double>> todosValorest_test = new ArrayList<>();
        List<List<Double>> todosValorest_train = new ArrayList<>();
        List<List<Double>> todosValorest_selec = new ArrayList<>();

        do {
            String pathExec = prePath + execution + indiceExec + File.separator + folderResults;
            diretorioExec = new File(pathExec);
            if (diretorioExec.exists()) {
                File diretorioIt;
                indiceIt = 0;

                List<Double> accsIt = new ArrayList<>();
                List<Double> t_testsIt = new ArrayList<>();
                List<Double> t_trainsIt = new ArrayList<>();
                List<Double> t_selecsIt = new ArrayList<>();

                do {
                    String pathIt = pathExec + File.separator + "it" + indiceIt;
                    diretorioIt = new File(pathIt);
                    if (diretorioIt.exists()) {

                        double t_selec = 0, acc = 0, t_test = 0, t_train = 0;
                        
                        if (svmBased) {
                            t_selec = Double.valueOf(io.open(pathIt + File.separator + "output.txt").get(1).split("\t")[3]);
                            acc = Double.valueOf(io.open(pathIt + File.separator + "output.txt").get(1).split("\t")[2]);
                            t_test = Double.valueOf(io.open(pathIt + File.separator + "output.txt").get(1).split("\t")[1]);
                            t_train = Double.valueOf(io.open(pathIt + File.separator + "output.txt").get(1).split("\t")[0]);
                        } else if (opfBased) {
                            acc = Double.valueOf(io.open(pathIt + File.separator + "testing.acc").get(0));
                            t_test = Double.valueOf(io.open(pathIt + File.separator + "testing.time").get(0));
                            t_train = Double.valueOf(io.open(pathIt + File.separator + trainingFileName).get(0));
                            t_selec = Double.valueOf(io.open(pathIt + File.separator + "OutrasInfos.txt").get(1).split("\t")[0]);
                        }

                        accsIt.add(acc);
                        t_testsIt.add(t_test);
                        t_trainsIt.add(t_train);
                        t_selecsIt.add(t_selec);

                        if (indiceExec == 0) {
                            sumAccs.add(acc);
                            sumt_tests.add(t_test);
                            sumt_trains.add(t_train);
                            sumt_selecs.add(t_selec);
                        } else {
                            sumAccs.set(indiceIt, sumAccs.get(indiceIt) + acc);
                            sumt_tests.set(indiceIt, sumt_tests.get(indiceIt) + t_test);
                            sumt_trains.set(indiceIt, sumt_trains.get(indiceIt) + t_train);
                            sumt_selecs.set(indiceIt, sumt_selecs.get(indiceIt) + t_selec);
                        }
                    }
                    indiceIt++;
                } while (diretorioIt.exists());

                todosValoresAcc.add(accsIt);
                todosValorest_test.add(t_testsIt);
                todosValorest_train.add(t_trainsIt);
                todosValorest_selec.add(t_selecsIt);

            }
            indiceExec++;
        } while (diretorioExec.exists());

        List<Double> dpAcc = desvioPadrao(indiceIt, indiceExec, todosValoresAcc, sumAccs);
        List<Double> dpt_test = desvioPadrao(indiceIt, indiceExec, todosValorest_test, sumt_tests);
        List<Double> dpt_train = desvioPadrao(indiceIt, indiceExec, todosValorest_train, sumt_trains);
        List<Double> dpt_selec = desvioPadrao(indiceIt, indiceExec, todosValorest_selec, sumt_selecs);

        System.out.println("AVERAGES");
        System.out.println("it(#)\tacc(%)\tt_test(s)\tt_train(s)\tt_selec(s)");
        for (int i = 0; i < sumAccs.size(); i++) {
            System.out.printf("%d\t%.2f\t%.6f\t%.6f\t%.6f\n", (i + 1), (sumAccs.get(i) / (indiceExec - 1)), (sumt_tests.get(i) / (indiceExec - 1)), (sumt_trains.get(i) / (indiceExec - 1)), (sumt_selecs.get(i) / (indiceExec - 1)));
        }
        System.out.println("");
        System.out.println("STD DEVS");
        System.out.println("it(#)\tacc(%)\tt_test(s)\tt_train(s)\tt_selec(s)");
        for (int i = 0; i < dpAcc.size(); i++) {
            System.out.printf("%d\t%.2f\t%.6f\t%.6f\t%.6f\n", (i + 1), dpAcc.get(i), dpt_test.get(i), dpt_train.get(i), dpt_selec.get(i));
        }

    }

    private static List<Double> desvioPadrao(int indiceIt, int indiceExec,
            List<List<Double>> todosValores, List<Double> sum) {

        List<Double> dp = new ArrayList<>();
        for (int i = 0; i < indiceIt - 1; i++) {
            double sumPow = 0.0;
            for (int j = 0; j < indiceExec - 1; j++) {
                sumPow += Math.pow((todosValores.get(j).get(i) - sum.get(i) / (indiceExec - 1)), 2);
            }
            double var = (sumPow / (indiceExec - 1));
            dp.add(Math.sqrt(var));
        }
        return dp;
    }
}
