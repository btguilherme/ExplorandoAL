/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package learning;

import classificar.ClassificadorOPF;
import classificar.ClassificadorOPFSemi;
import classificar.ClassificadorSVM;
import classificar.ClassificadorSVMGridSearch;
import classificar.ClassificadorUniverSVM;
import io.IOText;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.Movimentacao;
import weka.core.Instances;

/**
 *
 * @author guilherme
 */
public class Learn {

    protected void classify(String classifiers, int iteration, Instances raizes,
            Instances z3, int folds, double time, int numClassesConhecidas, int numCorrecoes) {
        //opfsuper svmcross svmgrid opfsemi universvm
        if (classifiers.contains("opfsuper")) {
            classifyOPF(iteration, time, numCorrecoes);
        }
        if (classifiers.contains("svmcross")) {
            classifySVM(raizes, z3, folds, time, iteration, numClassesConhecidas, numCorrecoes);
        }
        if (classifiers.contains("svmgrid")) {
            classifyGrid(raizes, z3, folds, time, iteration, numClassesConhecidas, numCorrecoes);
        }
        if (classifiers.contains("opfsemi")) {
            classifyOPFSemi(iteration, time, numCorrecoes);
        }
        if (classifiers.contains("universvm")) {
            classifyUniverSVM(folds, time, iteration, numClassesConhecidas, numCorrecoes);
        }
    }

    private void classifyOPF(int iteration, double time, int numCorrecoes) {
        IOText io = new IOText();
        ClassificadorOPF classificadorOPF = new ClassificadorOPF();
        classificadorOPF.makeTheSteps("raizes" + iteration);
        List<String> tempOPF = new ArrayList<>();
        tempOPF.add("t_select\tn_corrections");
        tempOPF.add(String.valueOf(time)+"\t"+String.valueOf(numCorrecoes));
        io.save(System.getProperty("user.dir").concat(File.separator), "OutrasInfos", tempOPF);
        moveFilesOPF(iteration);
    }

    private void classifyOPFSemi(int iteration, double time, int numCorrecoes) {
        IOText io = new IOText();
        ClassificadorOPFSemi classificadorOPFSemi = new ClassificadorOPFSemi();
        classificadorOPFSemi.makeTheSteps("raizes" + iteration);
        List<String> tempOPF = new ArrayList<>();
        tempOPF.add("t_select\tn_corrections");
        tempOPF.add(String.valueOf(time)+"\t"+String.valueOf(numCorrecoes));
        io.save(System.getProperty("user.dir").concat(File.separator), "OutrasInfos", tempOPF);
        moveFilesOPFSemi(iteration);
    }

    private void classifySVM(Instances raizes, Instances z3, int folds,
            double time, int iteration, int numClassesConhecidas, int numCorrecoes) {
        IOText ioText = new IOText();
        ClassificadorSVM classificadorSVM = new ClassificadorSVM();
        String resultsSVM = "train(s)\tclassify(s)\taccuracy(%)\tt_selection(s)\tZ1_length\tknow_classes\tn_corrections\n";
        resultsSVM = resultsSVM.concat(classificadorSVM.makeTheSteps(raizes, z3, folds));
        resultsSVM = resultsSVM.concat(String.valueOf(time) + "\t");
        resultsSVM = resultsSVM.concat(raizes.numInstances() + "\t");
        resultsSVM = resultsSVM.concat(numClassesConhecidas + "\t");
        resultsSVM = resultsSVM.concat(numCorrecoes + "\t");
        List<String> tempSVM = new ArrayList<>();
        tempSVM.add(resultsSVM);
        ioText.save(System.getProperty("user.dir").concat(File.separator), "output", tempSVM);
        moveFilesSVM_Grid(iteration, "svm_results");
    }

    private void classifyGrid(Instances raizes, Instances z3, int folds,
            double time, int iteration, int numClassesConhecidas, int numCorrecoes) {
        IOText ioText = new IOText();
        ClassificadorSVMGridSearch classificadorGrid = new ClassificadorSVMGridSearch();
        String resultsGrid = "train(s)\tclassify(s)\taccuracy(%)\tt_selection(s)\tZ1_length\tknow_classes\tn_corrections\n";
        resultsGrid = resultsGrid.concat(classificadorGrid.makeTheSteps(raizes, z3, folds));
        resultsGrid = resultsGrid.concat(String.valueOf(time) + "\t");
        resultsGrid = resultsGrid.concat(raizes.numInstances() + "\t");
        resultsGrid = resultsGrid.concat(numClassesConhecidas + "\t");
        resultsGrid = resultsGrid.concat(numCorrecoes + "\t");
        List<String> tempGrid = new ArrayList<>();
        tempGrid.add(resultsGrid);
        ioText.save(System.getProperty("user.dir").concat(File.separator), "output", tempGrid);
        moveFilesSVM_Grid(iteration, "grid_results");
    }

    private void classifyUniverSVM(int folds, double time, int iteration,
            int numClassesConhecidas, int numCorrecoes) {

        ClassificadorUniverSVM classificadorUniver = new ClassificadorUniverSVM(folds);
        classificadorUniver.makeTheSteps("raizes" + iteration);

        String resultsUniverSVM = "train(s)\tclassify(s)\taccuracy(%)\tt_selection(s)\tZ1_length\tknow_classes\tn_corrections\n";
        resultsUniverSVM = resultsUniverSVM.concat(dadosOutputUniverSVM());
//        //tempo treinamento, tempo classificação, accuracia, 
//        resultsUniverSVM = resultsUniverSVM.concat(String.valueOf(time) + "\t");
//        resultsUniverSVM = resultsUniverSVM.concat(raizes.numInstances() + "\t");
//        resultsUniverSVM = resultsUniverSVM.concat(numClassesConhecidas + "\t");
        //        resultsUniverSVM = resultsUniverSVM.concat(numCorrecoes + "\t");
//        List<String> tempSVM = new ArrayList<>();
//        tempSVM.add(resultsUniverSVM);
//        ioText.save(System.getProperty("user.dir").concat(File.separator), "output", tempSVM);
        moveFilesUniverSVM(iteration);
    }

    private String dadosOutputUniverSVM() {

        IOText io = new IOText();
        List<String> lines = io.open(System.getProperty("user.dir")
                .concat(File.separator).concat("outputUniverSVM.txt"));

        double acc = 0.0;
        double t_ = 0.0;

        for (String line : lines) {
            if (line.contains("Mean Cross Validation Accuracy:")) {
                acc = Double.parseDouble(line.split(":")[1]);
            }
        }

        return "";
    }

    private void moveFilesOPFSemi(int iteration) {
        String command = "mkdir opfsemi_results/it" + iteration;
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException ex) {
            Logger.getLogger(Learn.class.getName()).log(Level.SEVERE, null, ex);
        }

        String preSrc = System.getProperty("user.dir").concat(File.separator);
        String dst = preSrc + "opfsemi_results/it" + iteration;

        Movimentacao.exec("mv", preSrc + "classifier.opf", dst);
        Movimentacao.exec("mv", preSrc + "testing", dst);
        Movimentacao.exec("mv", preSrc + "testing.acc", dst);
        Movimentacao.exec("mv", preSrc + "testing.out", dst);
        Movimentacao.exec("mv", preSrc + "testing.time", dst);
        Movimentacao.exec("mv", preSrc + "Z1DOUBLELINE", dst);
        Movimentacao.exec("mv", preSrc + "Z1LINE", dst);
        Movimentacao.exec("mv", preSrc + "Z1LINE.out", dst);
        Movimentacao.exec("mv", preSrc + "Z1LINE.time", dst);
        Movimentacao.exec("mv", preSrc + "OutrasInfos.txt", dst);

        Movimentacao.exec("cp", preSrc + "raizes" + iteration + ".arff", dst);
    }

    private void moveFilesOPF(int iteration) {
        String command = "mkdir opf_results/it" + iteration;
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException ex) {
            Logger.getLogger(Learn.class.getName()).log(Level.SEVERE, null, ex);
        }

        String preSrc = System.getProperty("user.dir").concat(File.separator);
        String dst = preSrc + "opf_results/it" + iteration;

        Movimentacao.exec("mv", preSrc + "classifier.opf", dst);
        Movimentacao.exec("mv", preSrc + "raizes" + iteration + ".arff_train.opf", dst);
        Movimentacao.exec("mv", preSrc + "testing", dst);
        Movimentacao.exec("mv", preSrc + "testing.acc", dst);
        Movimentacao.exec("mv", preSrc + "testing.out", dst);
        Movimentacao.exec("mv", preSrc + "testing.time", dst);
        Movimentacao.exec("mv", preSrc + "training", dst);
        Movimentacao.exec("mv", preSrc + "training.out", dst);
        Movimentacao.exec("mv", preSrc + "training.time", dst);
        Movimentacao.exec("mv", preSrc + "OutrasInfos.txt", dst);

        Movimentacao.exec("cp", preSrc + "raizes" + iteration + ".arff", dst);
    }

    private void moveFilesSVM_Grid(int iteration, String folder) {
        String command = "mkdir " + folder + "/it" + iteration;
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException ex) {
            Logger.getLogger(Learn.class.getName()).log(Level.SEVERE, null, ex);
        }

        String preSrc = System.getProperty("user.dir").concat(File.separator);
        String dst = preSrc + folder + "/it" + iteration;

        Movimentacao.exec("mv", preSrc + "output.txt", dst);

        Movimentacao.exec("cp", preSrc + "raizes" + iteration + ".arff", dst);
        Movimentacao.exec("cp", preSrc + "splited".concat(File.separator).concat("teste.arff"), dst);
    }

    private void moveFilesUniverSVM(int iteration) {

        String command = "mkdir universvm_results/it" + iteration;
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException ex) {
            Logger.getLogger(Learn.class.getName()).log(Level.SEVERE, null, ex);
        }

        String preSrc = System.getProperty("user.dir").concat(File.separator);
        String dst = preSrc + "universvm_results/it" + iteration;

        Movimentacao.exec("mv", preSrc + "classifier.opf", dst);
        Movimentacao.exec("mv", preSrc + "labeled.svm.txt", dst);
        Movimentacao.exec("mv", preSrc + "outputUniverSVM.txt", dst);
        Movimentacao.exec("mv", preSrc + "splited".concat(File.separator).concat("teste.arff_test.svm.txt"), dst);
        Movimentacao.exec("mv", preSrc + "raizes" + iteration + ".arff_train.opf", dst);
        Movimentacao.exec("mv", preSrc + "unlabeled.svm.txt", dst);

        Movimentacao.exec("cp", preSrc + "raizes" + iteration + ".arff", dst);
    }
    
    public int numClassesConhecidas(Instances raizes) {
        Set<Integer> classes = new HashSet<>();
        for (int i = 0; i < raizes.numInstances(); i++) {
            String inst = raizes.instance(i).toString();
            int value = Integer.valueOf(inst.split(",")[inst.split(",").length - 1]);
            classes.add(value);
        }
        return classes.size();
    }
}
