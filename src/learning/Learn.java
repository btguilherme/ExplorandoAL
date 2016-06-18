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
import io.IOArff;
import io.IOText;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import raizes.Raizes;
import utils.RunCommand;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.SimpleKMeans;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.neighboursearch.KDTree;
import weka.filters.Filter;

/**
 *
 * @author guilherme
 */
public class Learn {

    public void active(Instances z2, Instances z3, int folds, int xNumClasses,
            String classifiers, int kVizinhos) {

        IOArff ioArff = new IOArff();

        int numInstancias = z2.attribute(z2.numAttributes() - 1).numValues() * xNumClasses;
        int iteration = 0;

        List<Object> temp = criaCluster(numInstancias, z2);
        SimpleKMeans clusterer = (SimpleKMeans) temp.get(0);
        //indice é o valor do cluster, e o valor em cada posicao
        //representa a classe
        List<Double> dicionario = (List<Double>) temp.get(1);

        Instances centroids = clusterer.getClusterCentroids();
        Instances raizes = raizesProximasAoCentroide(centroids, z2);

        //remove as instancias raizes de z2 (remove duplicatas)
        //mantem as classes
        temp = atualizaZ2(z2, raizes);
        z2 = (Instances) temp.get(0);

        List<BeanAmostra> fronteirasTemp = new ArrayList<>();

        int numAmostrasZ2 = z2.numInstances();

        do {
            /*
             primeira iteracao (iteration == 0) é utilizada apenas as amostras raizes
             na segunda iteracao (iteration == 1) as amostras de fronteira são selecionadas.
             para demais iteracoes, amostras são selecionadas do conjunto de amostras de fronteira
             */
            if (iteration == 1) {
                //fronteirasTemp sem classe
                fronteirasTemp = selecionaAmostrasFronteira(z2, clusterer, kVizinhos);
            }

            long init = System.nanoTime();

            if (iteration != 0) {
                int numFronteira = clusterer.getNumClusters();//2xnclass

                if (numFronteira > fronteirasTemp.size()) {
                    numFronteira = fronteirasTemp.size();
                }

                numAmostrasZ2 = numAmostrasZ2 - numFronteira;

                temp = selecionaAmostrasDaFronteira(z2, numFronteira, fronteirasTemp);
                z2 = (Instances) temp.get(0);

                Instances amostrasFronteira = (Instances) temp.get(1);
                fronteirasTemp = (List<BeanAmostra>) temp.get(2);
                Instances z2ExcluidosComClasse = (Instances) temp.get(3);

                temp = corrigeRotulos(amostrasFronteira, z2ExcluidosComClasse, clusterer, dicionario);

                if (amostrasFronteira.numInstances() != 0) {
                    Instances novasAmostrasRaizes = (Instances) temp.get(0);
                    for (int i = 0; i < novasAmostrasRaizes.numInstances(); i++) {
                        raizes.add(novasAmostrasRaizes.instance(i));
                    }
                }

                int numCorrecoes = (int) temp.get(1);
            }
            long end = System.nanoTime();
            long diff = end - init;
            double time = (diff / 1000000000.0);//tempo de selecao

            int numClassesConhecidas = 0;//numClassesConhecidas(dicionario, raizes);

            ioArff.saveArffFile(raizes, "raizes" + iteration);

            classify(classifiers, iteration, raizes, z3, folds, time, numClassesConhecidas);

            if (iteration != 0 && (fronteirasTemp.size() - numInstancias) < numInstancias) {
                break;
            }

            iteration++;

        } while (true);

    }

    public void random(Instances z2, Instances z3, int folds,
            int xNumClasses, String classifiers) {

        IOArff ioArff = new IOArff();

        int numInstancias = z2.attribute(z2.numAttributes() - 1).numValues() * xNumClasses;
        int iteration = 0;

        Raizes r = new Raizes();
        List<Instances> conjuntos = r.shuffle(z2, xNumClasses);
        Instances raizes = conjuntos.get(0);

        //atualiza z2 (amostras de z2 original menos amostras raizes de z1)
        z2 = conjuntos.get(1);

        do {
            long init = System.nanoTime();
            if (iteration != 0) {
                for (int i = 0; i < numInstancias; i++) {
                    raizes.add(z2.instance(i));
                }
                for (int i = 0; i < numInstancias; i++) {
                    z2.delete(i);
                }
            }
            long end = System.nanoTime();
            long diff = end - init;
            double time = (diff / 1000000000.0);//tempo de selecao

            int numClassesConhecidas = numClassesConhecidas(raizes);

            ioArff.saveArffFile(raizes, "raizes" + iteration);

            classify(classifiers, iteration, raizes, z3, folds, time, numClassesConhecidas);

            iteration++;

            if ((z2.numInstances() - numInstancias) < numInstancias) {
                break;
            }

        } while (true);
    }

    private void moveFilesOPFSemi(int iteration) {
        String command = "mkdir opfsemi_results/it" + iteration;
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException ex) {
            Logger.getLogger(Learn.class.getName()).log(Level.SEVERE, null, ex);
        }

        command = "mv "
                + System.getProperty("user.dir").concat(File.separator).concat("classifier.opf") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("opfsemi_results/it" + iteration);
        RunCommand.runCommand(command);
        command = "mv "
                + System.getProperty("user.dir").concat(File.separator).concat("testing") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("opfsemi_results/it" + iteration);
        RunCommand.runCommand(command);
        command = "mv "
                + System.getProperty("user.dir").concat(File.separator).concat("testing.acc") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("opfsemi_results/it" + iteration);
        RunCommand.runCommand(command);
        command = "mv "
                + System.getProperty("user.dir").concat(File.separator).concat("testing.out") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("opfsemi_results/it" + iteration);
        RunCommand.runCommand(command);
        command = "mv "
                + System.getProperty("user.dir").concat(File.separator).concat("testing.time") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("opfsemi_results/it" + iteration);
        RunCommand.runCommand(command);
        command = "mv "
                + System.getProperty("user.dir").concat(File.separator).concat("Z1DOUBLELINE") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("opfsemi_results/it" + iteration);
        RunCommand.runCommand(command);
        command = "mv "
                + System.getProperty("user.dir").concat(File.separator).concat("Z1LINE") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("opfsemi_results/it" + iteration);
        RunCommand.runCommand(command);
        command = "mv "
                + System.getProperty("user.dir").concat(File.separator).concat("Z1LINE.out") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("opfsemi_results/it" + iteration);
        RunCommand.runCommand(command);
        command = "mv "
                + System.getProperty("user.dir").concat(File.separator).concat("Z1LINE.time") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("opfsemi_results/it" + iteration);
        RunCommand.runCommand(command);
        command = "mv "
                + System.getProperty("user.dir").concat(File.separator).concat("selectTime.txt") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("opfsemi_results/it" + iteration);
        RunCommand.runCommand(command);

        command = "cp "
                + System.getProperty("user.dir").concat(File.separator).concat("raizes" + iteration + ".arff") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("opfsemi_results/it" + iteration);
        RunCommand.runCommand(command);

    }

    private void moveFilesOPF(int iteration) {
        String command = "mkdir opf_results/it" + iteration;
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException ex) {
            Logger.getLogger(Learn.class.getName()).log(Level.SEVERE, null, ex);
        }

        command = "mv "
                + System.getProperty("user.dir").concat(File.separator).concat("classifier.opf") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("opf_results/it" + iteration);
        RunCommand.runCommand(command);
        command = "mv "
                + System.getProperty("user.dir").concat(File.separator).concat("raizes" + iteration + ".arff_train.opf") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("opf_results/it" + iteration);
        RunCommand.runCommand(command);
        command = "mv "
                + System.getProperty("user.dir").concat(File.separator).concat("testing") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("opf_results/it" + iteration);
        RunCommand.runCommand(command);
        command = "mv "
                + System.getProperty("user.dir").concat(File.separator).concat("testing.acc") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("opf_results/it" + iteration);
        RunCommand.runCommand(command);
        command = "mv "
                + System.getProperty("user.dir").concat(File.separator).concat("testing.out") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("opf_results/it" + iteration);
        RunCommand.runCommand(command);
        command = "mv "
                + System.getProperty("user.dir").concat(File.separator).concat("testing.time") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("opf_results/it" + iteration);
        RunCommand.runCommand(command);
        command = "mv "
                + System.getProperty("user.dir").concat(File.separator).concat("training") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("opf_results/it" + iteration);
        RunCommand.runCommand(command);
        command = "mv "
                + System.getProperty("user.dir").concat(File.separator).concat("training.out") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("opf_results/it" + iteration);
        RunCommand.runCommand(command);
        command = "mv "
                + System.getProperty("user.dir").concat(File.separator).concat("training.time") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("opf_results/it" + iteration);
        RunCommand.runCommand(command);
        command = "mv "
                + System.getProperty("user.dir").concat(File.separator).concat("selectTime.txt") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("opf_results/it" + iteration);
        RunCommand.runCommand(command);

        command = "cp "
                + System.getProperty("user.dir").concat(File.separator).concat("raizes" + iteration + ".arff") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("opf_results/it" + iteration);
        RunCommand.runCommand(command);

    }

    private void moveFilesSVM_Grid(int iteration, String folder) {
        String command = "mkdir " + folder + "/it" + iteration;
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException ex) {
            Logger.getLogger(Learn.class.getName()).log(Level.SEVERE, null, ex);
        }

        command = "mv "
                + System.getProperty("user.dir").concat(File.separator).concat("output.txt") + " "
                + System.getProperty("user.dir").concat(File.separator).concat(folder + "/it" + iteration);
        RunCommand.runCommand(command);

        command = "cp "
                + System.getProperty("user.dir").concat(File.separator).concat("raizes" + iteration + ".arff") + " "
                + System.getProperty("user.dir").concat(File.separator).concat(folder + "/it" + iteration);
        RunCommand.runCommand(command);
        command = "cp "
                + System.getProperty("user.dir").concat(File.separator).concat("splited").concat(File.separator).concat("teste.arff") + " "
                + System.getProperty("user.dir").concat(File.separator).concat(folder + "/it" + iteration);
        RunCommand.runCommand(command);
    }

    private void moveFilesUniverSVM(int iteration) {

        String command = "mkdir universvm_results/it" + iteration;
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException ex) {
            Logger.getLogger(Learn.class.getName()).log(Level.SEVERE, null, ex);
        }

        command = "mv "
                + System.getProperty("user.dir").concat(File.separator).concat("classifier.opf") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("universvm_results/it" + iteration);
        RunCommand.runCommand(command);
        command = "mv "
                + System.getProperty("user.dir").concat(File.separator).concat("labeled.svm.txt") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("universvm_results/it" + iteration);
        RunCommand.runCommand(command);
        command = "mv "
                + System.getProperty("user.dir").concat(File.separator).concat("outputUniverSVM.txt") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("universvm_results/it" + iteration);
        RunCommand.runCommand(command);
        command = "mv "
                + System.getProperty("user.dir").concat(File.separator).concat("splited").concat(File.separator).concat("teste.arff_test.svm.txt") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("universvm_results/it" + iteration);
        RunCommand.runCommand(command);
        command = "mv "
                + System.getProperty("user.dir").concat(File.separator).concat("raizes" + iteration + ".arff_train.opf") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("universvm_results/it" + iteration);
        RunCommand.runCommand(command);
        command = "mv "
                + System.getProperty("user.dir").concat(File.separator).concat("unlabeled.svm.txt") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("universvm_results/it" + iteration);
        RunCommand.runCommand(command);

        command = "cp "
                + System.getProperty("user.dir").concat(File.separator).concat("raizes" + iteration + ".arff") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("universvm_results/it" + iteration);
        RunCommand.runCommand(command);

    }

    private void classify(String classifiers, int iteration, Instances raizes,
            Instances z3, int folds, double time, int numClassesConhecidas) {

        //opfsuper svmcross svmgrid opfsemi universvm
        if (classifiers.contains("opfsuper")) {
            classifyOPF(iteration, time);
        }
        if (classifiers.contains("svmcross")) {
            classifySVM(raizes, z3, folds, time, iteration, numClassesConhecidas);
        }
        if (classifiers.contains("svmgrid")) {
            classifyGrid(raizes, z3, folds, time, iteration, numClassesConhecidas);
        }
        if (classifiers.contains("opfsemi")) {
            classifyOPFSemi(iteration, time);
        }
        if (classifiers.contains("universvm")) {
            classifyUniverSVM(folds, time, iteration, numClassesConhecidas);
        }

    }

    private void classifyUniverSVM(int folds, double time, int iteration,
            int numClassesConhecidas) {

        ClassificadorUniverSVM classificadorUniver = new ClassificadorUniverSVM(folds);
        classificadorUniver.makeTheSteps("raizes" + iteration);

        String resultsUniverSVM = "train(s)\tclassify(s)\taccuracy(%)\tt_selection(s)\tZ1_length\tknow_classes\n";
        resultsUniverSVM = resultsUniverSVM.concat(dadosOutputUniverSVM());
//        //tempo treinamento, tempo classificação, accuracia, 
//        resultsUniverSVM = resultsUniverSVM.concat(String.valueOf(time) + "\t");
//        resultsUniverSVM = resultsUniverSVM.concat(raizes.numInstances() + "\t");
//        resultsUniverSVM = resultsUniverSVM.concat(numClassesConhecidas + "\t");
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

    private void classifyOPF(int iteration, double time) {
        IOText io = new IOText();
        ClassificadorOPF classificadorOPF = new ClassificadorOPF();
        classificadorOPF.makeTheSteps("raizes" + iteration);
        List<String> tempOPF = new ArrayList<>();
        tempOPF.add(String.valueOf(time));
        io.save(System.getProperty("user.dir").concat(File.separator), "selectTime", tempOPF);
        moveFilesOPF(iteration);
    }

    private void classifyOPFSemi(int iteration, double time) {
        IOText io = new IOText();
        ClassificadorOPFSemi classificadorOPFSemi = new ClassificadorOPFSemi();
        classificadorOPFSemi.makeTheSteps("raizes" + iteration);
        List<String> tempOPF = new ArrayList<>();
        tempOPF.add(String.valueOf(time));
        io.save(System.getProperty("user.dir").concat(File.separator), "selectTime", tempOPF);
        moveFilesOPFSemi(iteration);
    }

    private void classifySVM(Instances raizes, Instances z3, int folds,
            double time, int iteration, int numClassesConhecidas) {
        IOText ioText = new IOText();
        ClassificadorSVM classificadorSVM = new ClassificadorSVM();
        String resultsSVM = "train(s)\tclassify(s)\taccuracy(%)\tt_selection(s)\tZ1_length\tknow_classes\n";
        resultsSVM = resultsSVM.concat(classificadorSVM.makeTheSteps(raizes, z3, folds));
        resultsSVM = resultsSVM.concat(String.valueOf(time) + "\t");
        resultsSVM = resultsSVM.concat(raizes.numInstances() + "\t");
        resultsSVM = resultsSVM.concat(numClassesConhecidas + "\t");
        List<String> tempSVM = new ArrayList<>();
        tempSVM.add(resultsSVM);
        ioText.save(System.getProperty("user.dir").concat(File.separator), "output", tempSVM);
        moveFilesSVM_Grid(iteration, "svm_results");
    }

    private void classifyGrid(Instances raizes, Instances z3, int folds,
            double time, int iteration, int numClassesConhecidas) {
        IOText ioText = new IOText();
        ClassificadorSVMGridSearch classificadorGrid = new ClassificadorSVMGridSearch();
        String resultsGrid = "train(s)\tclassify(s)\taccuracy(%)\tt_selection(s)\tZ1_length\tknow_classes\n";
        resultsGrid = resultsGrid.concat(classificadorGrid.makeTheSteps(raizes, z3, folds));
        resultsGrid = resultsGrid.concat(String.valueOf(time) + "\t");
        resultsGrid = resultsGrid.concat(raizes.numInstances() + "\t");
        resultsGrid = resultsGrid.concat(numClassesConhecidas + "\t");
        List<String> tempGrid = new ArrayList<>();
        tempGrid.add(resultsGrid);
        ioText.save(System.getProperty("user.dir").concat(File.separator), "output", tempGrid);
        moveFilesSVM_Grid(iteration, "grid_results");
    }

    ///////active
    private List<Object> criaCluster(int size, Instances z2) {

        Instances z2SemClasse = removeAtributoClasse(z2);

        SimpleKMeans clusterer = new SimpleKMeans();
        clusterer.setSeed(10);
        clusterer.setPreserveInstancesOrder(true);
        try {
            clusterer.setNumClusters(size);
            clusterer.setMaxIterations(500);
            clusterer.buildClusterer(z2SemClasse);
        } catch (Exception ex) {
            Logger.getLogger(Learn.class.getName()).log(Level.SEVERE, null, ex);
        }

        ClusterEvaluation eval = new ClusterEvaluation();
        eval.setClusterer(clusterer);

        try {
            eval.evaluateClusterer(z2);
        } catch (Exception ex) {
            Logger.getLogger(Learn.class.getName()).log(Level.SEVERE, null, ex);
        }

        int[] ass = eval.getClassesToClusters();
        List<Double> assignments = new ArrayList<>();
        for (int i = 0; i < ass.length; i++) {
            assignments.add(Double.valueOf(ass[i]));
        }

        List<Object> ret = new ArrayList<>();
        ret.add(clusterer);
        ret.add(assignments);

        return ret;
    }

    private Map makeDic(SimpleKMeans clusterer) {
        String clusterInfo = clusterer.toString();
        String[] separados = clusterInfo.split("\n");

        String[] _dadosCluster = separados[10].split(" ");
        String[] _dadosClasses = separados[separados.length - 1].split(" ");

        List<Integer> dadosCluster = new ArrayList<>();
        List<String> dadosClasses = new ArrayList<>();

        for (int i = 0; i < _dadosCluster.length; i++) {
            if (!_dadosCluster[i].isEmpty()) {
                try {
                    dadosCluster.add(Integer.valueOf(_dadosCluster[i]));
                } catch (NumberFormatException e) {
                    //nao é numero
                }
            }
        }

        for (int i = 0; i < _dadosClasses.length; i++) {
            if (!_dadosClasses[i].isEmpty()) {
                dadosClasses.add(_dadosClasses[i]);
            }
        }

        dadosClasses.remove(0);
        dadosClasses.remove(0);

        Map dicionario = new HashMap();
        for (int i = 0; i < dadosClasses.size(); i++) {
            dicionario.put(dadosCluster.get(i), dadosClasses.get(i));
        }

        return dicionario;
    }

    private List<Integer> atribuicoes(SimpleKMeans clusterer) {
        int[] _assignments = null;
        try {
            _assignments = clusterer.getAssignments(); //mostra a qual cluster uma instancia pertence (predicted)
        } catch (Exception ex) {
            Logger.getLogger(Learn.class.getName()).log(Level.SEVERE, null, ex);
        }
        List<Integer> assignments = new ArrayList<>();
        for (int i = 0; i < _assignments.length; i++) {
            assignments.add(_assignments[i]);
        }
        return assignments;
    }

    private Instances raizesProximasAoCentroide(Instances centroids, Instances z2) {
        Instances z1 = new Instances(z2, centroids.numInstances());
        for (int i = 0; i < centroids.numInstances(); i++) {

            Instance inst = centroids.instance(i);
            KDTree tree = new KDTree();
            try {
                tree.setInstances(z2);
                z1.add(tree.kNearestNeighbours(inst, 1).firstInstance());
            } catch (Exception ex) {
                Logger.getLogger(Learn.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return z1;
    }

    private List<Object> atualizaZ2(Instances z2, Instances raizes) {

        Instances dataClusterer = removeAtributoClasse(z2);
        List<Integer> indicesApagar = new ArrayList<>();

        for (int i = 0; i < dataClusterer.numInstances(); i++) {
            for (int j = 0; j < raizes.numInstances(); j++) {
                if (dataClusterer.instance(i).toString().equals(raizes.instance(j).toString())) {
                    indicesApagar.add(i);
                }
            }
        }
        Collections.sort(indicesApagar);
        Collections.reverse(indicesApagar);
        for (int i = 0; i < indicesApagar.size(); i++) {
            z2.delete(indicesApagar.get(i));
        }
        indicesApagar.clear();

        List<Object> retorno = new ArrayList<>();
        retorno.add(z2);

        return retorno;
    }

    private List<Object> corrigeRotulos(Instances raizesSemClasse, Instances raizesComClasse,
            SimpleKMeans clusterer, List<Double> dicionario) {

        int corrigiu = 0;

        for (int i = 0; i < raizesSemClasse.numInstances(); i++) {
            Instance raizSemClasse = raizesSemClasse.instance(i);
            int clusterRaizSemClasse = 0;

            try {
                clusterRaizSemClasse = clusterer.clusterInstance(raizSemClasse);
            } catch (Exception ex) {
                Logger.getLogger(Learn.class.getName()).log(Level.SEVERE, null, ex);
            }

            for (int j = 0; j < raizesComClasse.numInstances(); j++) {
                Instance raizComClasse = raizesComClasse.instance(j);

                if (raizComClasse.toString().contains(raizSemClasse.toString())) {
                    double classeRaizComClasse = raizComClasse.classValue();
                    int clusterRaizComClasse = dicionario.indexOf(classeRaizComClasse);
                    if (clusterRaizComClasse != clusterRaizSemClasse) {
                        //não faz a correção em si. Apenas conta a quantidade de
                        //amostras que estão em diferentes clusters
                        //e retorna as amostras com classe
                        corrigiu++;
                    }
                    break;
                }
            }
        }

        List<Object> retorno = new ArrayList<>();
        retorno.add(raizesComClasse);
        retorno.add(corrigiu);

        return retorno;
    }

    private List<Object> selecionaAmostrasDaFronteira(Instances z2, int numFronteira,
            List<BeanAmostra> fronteirasTemp) {

        List<Integer> indicesConjZ2 = new ArrayList<>();
        Instances z2SemClasses = removeAtributoClasse(z2);
        Instances amostrasFronteira = new Instances(z2SemClasses, numFronteira);
        for (int i = 0; i < numFronteira; i++) {
            BeanAmostra ba = fronteirasTemp.get(i);
            Instance sampleT = ba.getAmostra();
            amostrasFronteira.add(sampleT);
            indicesConjZ2.add(ba.getIndiceZ2());
        }
        for (int i = 0; i < numFronteira; i++) {
            fronteirasTemp.remove(0);
        }

        Collections.sort(indicesConjZ2);
        Collections.reverse(indicesConjZ2);

        Instances z2ExcluidosComClasse = new Instances(z2, indicesConjZ2.size());

        for (Integer indice : indicesConjZ2) {
            z2ExcluidosComClasse.add(z2.instance(indice));
            //z2.delete(indice);
        }
        List<Object> retorno = new ArrayList<>();
        retorno.add(z2);
        retorno.add(amostrasFronteira);
        retorno.add(fronteirasTemp);
        retorno.add(z2ExcluidosComClasse);

        return retorno;
    }

    private Integer numClassesConhecidas(Map dicionario, Instances raizes) {
        int cont = 0;
        Set<Integer> classes = new HashSet<>();
        for (int i = 0; i < raizes.numInstances(); i++) {
            String inst = raizes.instance(i).toString();
            int value = Integer.valueOf(inst.split(",")[inst.split(",").length - 1]);
            classes.add(value);
        }
        for (Integer classe : classes) {
            if (dicionario.containsKey(classe)) {
                cont = cont + 1;
            }
        }
        classes.clear();
        return cont;
    }

    private int numClassesConhecidas(Instances raizes) {
        Set<Integer> classes = new HashSet<>();
        for (int i = 0; i < raizes.numInstances(); i++) {
            String inst = raizes.instance(i).toString();
            int value = Integer.valueOf(inst.split(",")[inst.split(",").length - 1]);
            classes.add(value);
        }
        return classes.size();
    }

    private List<BeanAmostra> ordenaAmostrasFronteira(List<BeanAmostra> amostrasT,
            List<BeanAmostra> vizinhosT, Instances z2) {

        List<BeanAmostra> temp = new ArrayList<>();
        Map<Integer, Double> kkk = new HashMap<>();

        for (int i = 0; i < amostrasT.size(); i++) {
            EuclideanDistance euclidean = new EuclideanDistance();
            euclidean.setInstances(z2);
            kkk.put(i, euclidean.distance(amostrasT.get(i).getAmostra(), vizinhosT.get(i).getAmostra()));
        }

        Map<Integer, Double> sorted = MapUtil.sortByValue(kkk);
        Set<Integer> values = sorted.keySet();

        for (Integer value : values) {
            temp.add(amostrasT.get(value));
        }

        return temp;
    }

    private List<BeanAmostra> selecionaAmostrasFronteira(Instances z2, SimpleKMeans clusterer, int kVizinhos) {

        Instances z2SemClasse = removeAtributoClasse(z2);

        List<BeanAmostra> fronteirasTemp = new ArrayList<>();
        List<BeanAmostra> amostrasT = new ArrayList<>();
        List<BeanAmostra> vizinhosT = new ArrayList<>();

        for (int i = 0; i < z2SemClasse.numInstances(); i++) {
            Instance t = z2SemClasse.instance(i);
            int clusterT = 0;
            try {
                clusterT = clusterer.clusterInstance(t);
            } catch (Exception ex) {
                Logger.getLogger(Learn.class.getName()).log(Level.SEVERE, null, ex);
            }

            KDTree tree = new KDTree();
            EuclideanDistance df = new EuclideanDistance(z2SemClasse);
            df.setDontNormalize(true);
            try {
                tree.setInstances(z2SemClasse);
                tree.setDistanceFunction(df);
            } catch (Exception ex) {
                Logger.getLogger(Learn.class.getName()).log(Level.SEVERE, null, ex);
            }

            Instances vizinhos = null; //aqui não tem classe
            try {
                vizinhos = tree.kNearestNeighbours(t, kVizinhos);
                vizinhos.setClassIndex(vizinhos.numAttributes() - 1);
            } catch (Exception ex) {
                Logger.getLogger(Learn.class.getName()).log(Level.SEVERE, null, ex);
            }

            for (int j = 0; j < vizinhos.numInstances(); j++) {
                int clusterV = 0;
                try {
                    clusterV = clusterer.clusterInstance(vizinhos.instance(j));
                } catch (Exception ex) {
                    Logger.getLogger(Learn.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (clusterT != clusterV) {//amostra de fronteira

                    BeanAmostra amostraT = new BeanAmostra(z2SemClasse.instance(i), clusterT, i);
                    BeanAmostra vizinhoT = new BeanAmostra(vizinhos.instance(j), 0, j);

                    amostrasT.add(amostraT);
                    vizinhosT.add(vizinhoT);

                    break;
                }
            }
        }

        //ordenar da menor distancia para a maior
        //amostras em fronteirasTemp não tem classe
        fronteirasTemp = ordenaAmostrasFronteira(amostrasT, vizinhosT, z2);
        return fronteirasTemp;
    }

    private Instances removeAtributoClasse(Instances z2) {
        Instances dataClusterer = null;
        weka.filters.unsupervised.attribute.Remove filter = new weka.filters.unsupervised.attribute.Remove();
        filter.setAttributeIndices("" + (z2.classIndex() + 1));

        try {
            filter.setInputFormat(z2);
            dataClusterer = Filter.useFilter(z2, filter);
        } catch (Exception ex) {
            Logger.getLogger(Learn.class.getName()).log(Level.SEVERE, null, ex);
        }

        return dataClusterer;
    }

}
