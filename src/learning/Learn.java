/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package learning;

import classificar.ClassificadorCollectiveWrapper;
import classificar.ClassificadorFilteredCollectiveClassifier;
import classificar.ClassificadorLLGC;
import classificar.ClassificadorOPF;
import classificar.ClassificadorOPFSemi;
import classificar.ClassificadorSVM;
import classificar.ClassificadorSVMGridSearch;
import classificar.ClassificadorUniverSVM;
import classificar.ClassificadorWeighting;
import classificar.ClassificadorYATSI;
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
import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 *
 * @author guilherme
 */
public class Learn {

    /**
     *
     * @param classifiers Lista de classificadores, sepearados por espaço
     * simples.
     * @param iteration Numero da iteraçao atual (servira para criar um
     * diretorio do tipo <code>iteration_X</code>, onde X e o valor em
     * <code>iteration</code>
     * @param raizes Instancias raizes ou raizes + fronteira, caso seja iteraçao
     * => 1
     * @param z3 Conjunto de teste
     * @param time Tempo de seleção de amostras de fronteira
     * @param numClassesConhecidas Número de classes conhecidas
     * @param numCorrecoes Número de amostras corrigidas
     */
//    protected void classify(String classifiers, int iteration, Instances raizes,
//            Instances z3, double time, int numClassesConhecidas, int numCorrecoes) {
//        //opfsuper svmcross svmgrid opfsemi universvm
//        if (classifiers.contains("opfsuper")) {
//            classifyOPF(raizes, iteration, time, numClassesConhecidas, numCorrecoes);
//        }
//        if (classifiers.contains("svmcross")) {
//            classifySVM(raizes, z3, time, iteration, numClassesConhecidas, numCorrecoes);
//        }
//        if (classifiers.contains("svmgrid")) {
//            classifyGrid(raizes, z3, time, iteration, numClassesConhecidas, numCorrecoes);
//        }
//        if (classifiers.contains("opfsemi")) {
//            classifyOPFSemi(raizes, iteration, time, numClassesConhecidas, numCorrecoes);
//        }
//        if (classifiers.contains("universvm")) {
//            classifyUniverSVM(raizes, time, iteration, numClassesConhecidas, numCorrecoes);
//        }
//    }
    protected Classifier classifier;
    protected ClassificadorSVM classificadorSVM;
    protected ClassificadorSVMGridSearch classificadorSVMGridSearch;
    protected ClassificadorOPF classificadorOPF;
    protected ClassificadorOPFSemi classificadorOPFSemi;
    protected ClassificadorLLGC classificadorLLGC;
    protected ClassificadorCollectiveWrapper classificadorCollectiveWrapper;
    protected ClassificadorWeighting classificadorWeighting;
    protected ClassificadorYATSI classificadorYATSI;
    protected ClassificadorFilteredCollectiveClassifier classificadorFilteredCollectiveClassifier;

    protected Set<String> classesConhecidas;
    
    protected boolean isSupervisionado;

    protected void classifica(String classificador, Instances raizes, Instances z3, Instances unlabeled) {

        int classificadasWrong;
        switch (classificador) {
            case "svmcross":
                classificadorSVM = new ClassificadorSVM();
                classifier = classificadorSVM.makeTheSteps(raizes, z3);
                break;
            case "svmgrid":
                classificadorSVMGridSearch = new ClassificadorSVMGridSearch();
                classifier = classificadorSVMGridSearch.makeTheSteps(raizes, z3);
                break;
            case "LLGC":
                classificadorLLGC = new ClassificadorLLGC();
                classifier = classificadorLLGC.makeTheSteps(raizes, z3, unlabeled);
                break;
            case "CollectiveWrapper":
                classificadorCollectiveWrapper = new ClassificadorCollectiveWrapper();
                classifier = classificadorCollectiveWrapper.makeTheSteps(raizes, z3, unlabeled);
                break;
            case "Weighting":
                classificadorWeighting = new ClassificadorWeighting();
                classifier = classificadorWeighting.makeTheSteps(raizes, z3, unlabeled);
                break;
            case "YATSI":
                classificadorYATSI = new ClassificadorYATSI();
                classifier = classificadorYATSI.makeTheSteps(raizes, z3, unlabeled);
                break;
            case "FilteredCollectiveClassifier":
                classificadorFilteredCollectiveClassifier = new ClassificadorFilteredCollectiveClassifier();
                classifier = classificadorFilteredCollectiveClassifier.makeTheSteps(raizes, z3, unlabeled);
                break;
        }
        classificadasWrong = verificaRaizesClassificadasErradas(raizes, classifier);
        new IOText().save(System.getProperty("user.dir").concat(File.separator),
                "classificadasErradas", String.valueOf(classificadasWrong + "/" + raizes.numInstances()));

    }

    protected void salvaDados(String classificador, int iteration) {
        System.err.print("Salvando dados ... ");

        switch (classificador) {
            case "svmcross":
                moveFilesSVM_Grid(iteration, "svm_results");
                break;
            case "svmgrid":
                moveFilesSVM_Grid(iteration, "grid_results");
                break;
            case "LLGC":
                moveFilesSVM_Grid(iteration, "LLGC_results");
                break;
            case "CollectiveWrapper":
                moveFilesSVM_Grid(iteration, "CollectiveWrapper_results");
                break;
            case "Weighting":
                moveFilesSVM_Grid(iteration, "Weighting_results");
                break;
            case "YATSI":
                moveFilesSVM_Grid(iteration, "YATSI_results");
                break;
            case "FilteredCollectiveClassifier":
                moveFilesSVM_Grid(iteration, "FilteredCollectiveClassifier_results");
                break;
        }
        System.err.println("feito");
    }

    protected int verificaRaizesClassificadasErradas(Instances amostras, Classifier classificador) {
        int contador = 0;
        try {
            for (int i = 0; i < amostras.numInstances(); i++) {
                double pred = classificador.classifyInstance(amostras.instance(i));
                String actual = amostras.classAttribute().value((int) amostras.instance(i).classValue());
                String predicted = amostras.classAttribute().value((int) pred);

                if (!actual.equals(predicted)) {
                    contador++;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(Learn.class.getName()).log(Level.SEVERE, null, ex);
        }
        return contador;
    }

    protected int classesConhecidas(Instances amostras) {
        for (int i = 0; i < amostras.numInstances(); i++) {
            String actual = amostras.classAttribute().value((int) amostras.instance(i).classValue());
            classesConhecidas.add(actual);
        }
        return classesConhecidas.size();
    }

    private void classifyOPF(Instances raizes, int iteration, double time,
            int numClassesConhecidas, int numCorrecoes) {

        ClassificadorOPF classificadorOPF = new ClassificadorOPF();
        classificadorOPF.makeTheSteps("raizes" + iteration);
        List<String> tempOPF = new ArrayList<>();
        tempOPF.add("t_select\tn_corrections\tknow_classes\tZ1_length");
        tempOPF.add(String.valueOf(time) + "\t" + String.valueOf(numCorrecoes) + "\t"
                + numClassesConhecidas + "\t" + String.valueOf(raizes.numInstances()));

        new IOText().save(System.getProperty("user.dir").concat(File.separator), "OutrasInfos", tempOPF);
        moveFilesOPF(iteration);
    }

    private void classifyOPFSemi(Instances raizes, int iteration, double time,
            int numClassesConhecidas, int numCorrecoes) {

        ClassificadorOPFSemi classificadorOPFSemi = new ClassificadorOPFSemi();
        classificadorOPFSemi.makeTheSteps("raizes" + iteration);
        List<String> tempOPF = new ArrayList<>();
        tempOPF.add("t_select\tn_corrections\tknow_classes\tZ1_length");
        tempOPF.add(String.valueOf(time) + "\t" + String.valueOf(numCorrecoes) + "\t"
                + numClassesConhecidas + "\t" + String.valueOf(raizes.numInstances()));

        new IOText().save(System.getProperty("user.dir").concat(File.separator), "OutrasInfos", tempOPF);
        moveFilesOPFSemi(iteration);
    }

    private void classifySVM(Instances raizes, Instances z3,
            double time, int iteration, int numClassesConhecidas, int numCorrecoes) {

        ClassificadorSVM classificadorSVM = new ClassificadorSVM();
        String resultsSVM = "train(s)\tclassify(s)\taccuracy(%)\tt_selection(s)\tZ1_length\tknow_classes\tn_corrections\n";
        //resultsSVM = resultsSVM.concat(classificadorSVM.makeTheSteps(raizes, z3));
        resultsSVM = resultsSVM.concat(String.valueOf(time) + "\t");
        resultsSVM = resultsSVM.concat(raizes.numInstances() + "\t");
        resultsSVM = resultsSVM.concat(numClassesConhecidas + "\t");
        resultsSVM = resultsSVM.concat(numCorrecoes + "\t");
        List<String> tempSVM = new ArrayList<>();
        tempSVM.add(resultsSVM);
        new IOText().save(System.getProperty("user.dir").concat(File.separator), "output", tempSVM);
        moveFilesSVM_Grid(iteration, "svm_results");
    }

    private void classifyGrid(Instances raizes, Instances z3,
            double time, int iteration, int numClassesConhecidas, int numCorrecoes) {

        ClassificadorSVMGridSearch classificadorGrid = new ClassificadorSVMGridSearch();
        String resultsGrid = "train(s)\tclassify(s)\taccuracy(%)\tt_selection(s)\tZ1_length\tknow_classes\tn_corrections\n";
        //resultsGrid = resultsGrid.concat(classificadorGrid.makeTheSteps(raizes, z3));
        resultsGrid = resultsGrid.concat(String.valueOf(time) + "\t");
        resultsGrid = resultsGrid.concat(raizes.numInstances() + "\t");
        resultsGrid = resultsGrid.concat(numClassesConhecidas + "\t");
        resultsGrid = resultsGrid.concat(numCorrecoes + "\t");
        List<String> tempGrid = new ArrayList<>();
        tempGrid.add(resultsGrid);
        new IOText().save(System.getProperty("user.dir").concat(File.separator), "output", tempGrid);
        moveFilesSVM_Grid(iteration, "grid_results");
    }

    private void classifyUniverSVM(Instances raizes, double time, int iteration,
            int numClassesConhecidas, int numCorrecoes) {

        ClassificadorUniverSVM classificadorUniver = new ClassificadorUniverSVM();
        classificadorUniver.makeTheSteps("raizes" + iteration);

        String resultsUniverSVM = "t_selection(s)\tZ1_length\tknow_classes\tn_corrections\n";
        resultsUniverSVM = resultsUniverSVM.concat(String.valueOf(time) + "\t");
        resultsUniverSVM = resultsUniverSVM.concat(raizes.numInstances() + "\t");
        resultsUniverSVM = resultsUniverSVM.concat(numClassesConhecidas + "\t");
        resultsUniverSVM = resultsUniverSVM.concat(numCorrecoes + "\t");
        List<String> tempSVM = new ArrayList<>();
        tempSVM.add(resultsUniverSVM);
        new IOText().save(System.getProperty("user.dir").concat(File.separator), "output", tempSVM);
        moveFilesUniverSVM(iteration);
    }

    protected void moveFilesOPFSemi(int iteration) {
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

        Movimentacao.exec("cp", preSrc + "tempoAgrupamento.txt", dst);
        Movimentacao.exec("cp", preSrc + "tempoSelecaoFronteira.txt", dst);
        Movimentacao.exec("cp", preSrc + "tempoOrdenacao.txt", dst);

        Movimentacao.exec("cp", preSrc + "raizes" + iteration + ".arff", dst);
    }

    protected void moveFilesOPF(int iteration) {
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

        Movimentacao.exec("cp", preSrc + "tempoAgrupamento.txt", dst);
        Movimentacao.exec("cp", preSrc + "tempoSelecaoFronteira.txt", dst);
        Movimentacao.exec("cp", preSrc + "tempoOrdenacao.txt", dst);

        Movimentacao.exec("cp", preSrc + "raizes" + iteration + ".arff", dst);
    }

    protected void moveFilesSVM_Grid(int iteration, String folder) {
        String command = "mkdir " + folder + "/it" + iteration;
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException ex) {
            Logger.getLogger(Learn.class.getName()).log(Level.SEVERE, null, ex);
        }

        String preSrc = System.getProperty("user.dir").concat(File.separator);
        String dst = preSrc + folder + "/it" + iteration;

        String dstFolderClassifier = preSrc + folder;

        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Logger.getLogger(Learn.class.getName()).log(Level.SEVERE, null, ex);
        }

        Movimentacao.exec("cp", preSrc + "tempoTeste.txt", dst);//
        Movimentacao.exec("cp", preSrc + "tempoTreino.txt", dst);//
        Movimentacao.exec("cp", preSrc + "acc.txt", dst);//
        Movimentacao.exec("cp", preSrc + "classificadasErradas.txt", dst);//
        Movimentacao.exec("cp", preSrc + "classesConhecidas.txt", dst);//
        Movimentacao.exec("cp", preSrc + "unlabeled.arff", dst);
        Movimentacao.exec("cp", preSrc + "tempoSelecao.txt", dst);//

        Movimentacao.exec("cp", preSrc + "amostrasDeFronteira.arff", dstFolderClassifier);
        Movimentacao.exec("cp", preSrc + "tempoAgrupamento.txt", dstFolderClassifier);
        Movimentacao.exec("cp", preSrc + "tempoOrdenacao.txt", dstFolderClassifier);
        Movimentacao.exec("cp", preSrc + "tempoSelecaoFronteira.txt", dstFolderClassifier);

        Movimentacao.exec("cp", preSrc + "raizes" + iteration + ".arff", dst);
        Movimentacao.exec("cp", preSrc + "splited".concat(File.separator).concat("teste.arff"), dst);
    }

    protected void moveFilesUniverSVM(int iteration) {

        String command = "mkdir universvm_results/it" + iteration;
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException ex) {
            Logger.getLogger(Learn.class.getName()).log(Level.SEVERE, null, ex);
        }

        String preSrc = System.getProperty("user.dir").concat(File.separator);
        String dst = preSrc + "universvm_results/it" + iteration;

        Movimentacao.exec("mv", preSrc + "acuracy.txt", dst);
        Movimentacao.exec("mv", preSrc + "labeled.svm.txt", dst);
        Movimentacao.exec("mv", preSrc + "output.txt", dst);
        Movimentacao.exec("mv", preSrc + "outputUniverSVM.txt", dst);
        Movimentacao.exec("mv", preSrc + "splited".concat(File.separator).concat("teste.arff_test.svm.txt"), dst);
        Movimentacao.exec("mv", preSrc + "testing.txt", dst);
        Movimentacao.exec("mv", preSrc + "unlabeled.svm.txt", dst);

        Movimentacao.exec("cp", preSrc + "tempoAgrupamento.txt", dst);
        Movimentacao.exec("cp", preSrc + "tempoSelecaoFronteira.txt", dst);
        Movimentacao.exec("cp", preSrc + "tempoOrdenacao.txt", dst);

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
    
    protected boolean tipoClassificador(String classificador) {
        boolean ret = false;
        switch (classificador) {
            case "svmcross":
            case "svmgrid":
                ret = true;
                break;
            case "LLGC":
            case "CollectiveWrapper":
            case "Weighting":
            case "YATSI":
            case "FilteredCollectiveClassifier":
                ret = false;
                break;
        }
        return ret;
    }
}
