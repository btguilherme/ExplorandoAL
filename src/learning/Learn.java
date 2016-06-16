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
import java.io.BufferedInputStream;
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
import weka.clusterers.SimpleKMeans;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.neighboursearch.KDTree;

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

        SimpleKMeans clusterer = criaCluster(numInstancias, z2);
        Map dicionario = makeDic(clusterer);
        List<Integer> assignments = atribuicoes(clusterer);

        Instances centroids = clusterer.getClusterCentroids();
        Instances raizes = raizesProximasAoCentroide(centroids, z2);
        raizes.setClassIndex(raizes.numAttributes() - 1);

        //remove as instancias raizes de z2
        List<Object> temp = atualizaZ2(z2, raizes, assignments);
        z2 = (Instances) temp.get(0);
        assignments = (List<Integer>) temp.get(1);

        do {
            long init = System.nanoTime();
            List<BeanAmostra> fronteirasTemp = new ArrayList<>();

            List<BeanAmostra> amostrasT = new ArrayList<>();
            List<BeanAmostra> vizinhosT = new ArrayList<>();

            if (iteration != 0) {

                for (int i = 0; i < z2.numInstances(); i++) {
                    Instance t = z2.instance(i);
                    int clusterT = assignments.get(i);

                    KDTree tree = new KDTree();
                    EuclideanDistance df = new EuclideanDistance(z2);
                    df.setDontNormalize(true);
                    try {
                        tree.setInstances(z2);
                        tree.setDistanceFunction(df);
                    } catch (Exception ex) {
                        Logger.getLogger(Learn.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    Instances vizinhos = null;
                    try {
                        vizinhos = tree.kNearestNeighbours(t, kVizinhos);
                    } catch (Exception ex) {
                        Logger.getLogger(Learn.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    loop:
                    for (int j = 0; j < vizinhos.numInstances(); j++) {
                        for (int l = 0; l < z2.numInstances(); l++) {
                            if (vizinhos.instance(j).toString().equals(z2.instance(l).toString())) {
                                int clusterV = assignments.get(l);
                                if (clusterT != clusterV) {//cluster diferente -> amostra de fronteira

//                                    BeanAmostra ba = new BeanAmostra(t, clusterT, i);
//                                    fronteirasTemp.add(ba);

                                    BeanAmostra amostraT = new BeanAmostra(t, clusterT, i);
                                    BeanAmostra vizinhoT = new BeanAmostra(vizinhos.instance(j), 0, j);

                                    amostrasT.add(amostraT);
                                    vizinhosT.add(vizinhoT);

                                    break loop;
                                }
                            }
                        }
                    }
                }

                //ordenar da menor distancia para a maior
                fronteirasTemp = ordenaAmostrasFronteira(amostrasT, vizinhosT, z2);
            }

            long end = System.nanoTime();
            long diff = end - init;
            double time = (diff / 1000000000.0);//tempo de selecao

            //Collections.reverse(fronteirasTemp);
            
            int numFronteira = clusterer.getNumClusters();//2xnclass

            if (numFronteira > fronteirasTemp.size()) {
                numFronteira = fronteirasTemp.size();
            }

            temp = selecionaRaizesDaFronteira(z2, numFronteira, fronteirasTemp, assignments);
            z2 = (Instances) temp.get(0);
            assignments = (List<Integer>) temp.get(1);
            Instances amostrasFronteira = (Instances) temp.get(2);

            temp = corrigeRotulos(amostrasFronteira, dicionario, clusterer);
            dicionario = (Map) temp.get(0);
            if (amostrasFronteira.numInstances() != 0) {
                Instances novasAmostrasRaizes = (Instances) temp.get(1);
                for (int i = 0; i < novasAmostrasRaizes.numInstances(); i++) {
                    raizes.add(novasAmostrasRaizes.instance(i));
                }
            }
            int numCorrecoes = (int) temp.get(2);

            int numClassesConhecidas = numClassesConhecidas(dicionario, raizes);

            ioArff.saveArffFile(raizes, "raizes" + iteration);

            classify(classifiers, iteration, raizes, z3, folds, time, numClassesConhecidas);

            iteration++;

            if ((z2.numInstances() - numInstancias) < numInstancias) {
                break;
            }

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

    private void runCommand(String comando) {
        try {

            Process p = Runtime.getRuntime().exec(comando);

            BufferedInputStream in = new BufferedInputStream(p.getInputStream());
            byte[] bytes = new byte[4096];
            while (in.read(bytes) != -1) {
                System.err.println("waiting " + comando);
            }

            p.waitFor();

            System.err.println("done  " + comando);

        } catch (IOException ex) {
            System.out.println("Comando não encontrado");
            System.exit(0);
        } catch (InterruptedException ex) {
            Logger.getLogger(ClassificadorOPF.class.getName()).log(Level.SEVERE, null, ex);
        }
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
                + System.getProperty("user.dir").concat(File.separator).concat("testing") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("testing.acc") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("testing.out") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("testing.time") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("Z1DOUBLELINE") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("Z1LINE") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("Z1LINE.out") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("Z1LINE.time") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("selectTime.txt") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("opfsemi_results/it" + iteration);
        runCommand(command);

        command = "cp "
                + System.getProperty("user.dir").concat(File.separator).concat("raizes" + iteration + ".arff") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("opfsemi_results/it" + iteration);
        runCommand(command);

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
                + System.getProperty("user.dir").concat(File.separator).concat("raizes" + iteration + ".arff_train.opf") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("teste.arff_test.opf") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("testing") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("testing.acc") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("testing.out") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("testing.time") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("training") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("training.out") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("training.time") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("selectTime.txt") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("opf_results/it" + iteration);
        runCommand(command);

        command = "cp "
                + System.getProperty("user.dir").concat(File.separator).concat("raizes" + iteration + ".arff") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("opf_results/it" + iteration);
        runCommand(command);

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
        runCommand(command);

        command = "cp "
                + System.getProperty("user.dir").concat(File.separator).concat("raizes" + iteration + ".arff") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("splited").concat(File.separator).concat("teste.arff") + " "
                + System.getProperty("user.dir").concat(File.separator).concat(folder + "/it" + iteration);
        runCommand(command);
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
                + System.getProperty("user.dir").concat(File.separator).concat("labeled.svm.txt") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("outputUniverSVM.txt") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("splited").concat(File.separator).concat("teste.arff_test.svm.txt") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("raizes" + iteration + ".arff_train.opf") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("unlabeled.svm.txt") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("universvm_results/it" + iteration);
        runCommand(command);

        command = "cp "
                + System.getProperty("user.dir").concat(File.separator).concat("raizes" + iteration + ".arff") + " "
                + System.getProperty("user.dir").concat(File.separator).concat("universvm_results/it" + iteration);
        runCommand(command);

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
            classifyUniverSVM(raizes, time, iteration, numClassesConhecidas);
        }

    }

    private void classifyUniverSVM(Instances raizes, double time, int iteration, int numClassesConhecidas) {

        ClassificadorUniverSVM classificadorUniver = new ClassificadorUniverSVM();
        classificadorUniver.makeTheSteps("raizes" + iteration);

//        String resultsUniverSVM = "train(s)\tclassify(s)\taccuracy(%)\tt_selection(s)\tZ1_length\tknow_classes\n";
//        resultsUniverSVM = resultsUniverSVM.concat(dadosOutputUniverSVM());
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
            if (line.contains("mean accuracy")) {
                acc = Double.parseDouble(line.split("=")[1]);
            } else if (line.contains("<y>_train")) {
                Double.parseDouble(line.split("=")[1]);
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
    private SimpleKMeans criaCluster(int size, Instances z2) {
        SimpleKMeans clusterer = new SimpleKMeans();
        clusterer.setSeed(10);
        clusterer.setPreserveInstancesOrder(true);
        try {
            clusterer.setNumClusters(size);
            clusterer.setMaxIterations(500);
            clusterer.buildClusterer(z2);
        } catch (Exception ex) {
            Logger.getLogger(Learn.class.getName()).log(Level.SEVERE, null, ex);
        }

        return clusterer;
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
        Instances z1 = new Instances(centroids, centroids.numInstances());

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

    private List<Object> atualizaZ2(Instances z2, Instances raizes, List<Integer> assignments) {
        List<Integer> indicesApagar = new ArrayList<>();
        for (int i = 0; i < z2.numInstances(); i++) {
            for (int j = 0; j < raizes.numInstances(); j++) {
                if (z2.instance(i).toString().equals(raizes.instance(j).toString())) {
                    indicesApagar.add(i);
                }
            }
        }
        Collections.sort(indicesApagar);
        Collections.reverse(indicesApagar);
        for (int i = 0; i < indicesApagar.size(); i++) {
            z2.delete(indicesApagar.get(i));
            assignments.remove(indicesApagar.get(i));
        }
        indicesApagar.clear();

        List<Object> retorno = new ArrayList<>();
        retorno.add(z2);
        retorno.add(assignments);

        return retorno;
    }

    private List<Object> corrigeRotulos(Instances raizes, Map dict, SimpleKMeans clusterer) {

        raizes.setClassIndex(raizes.numAttributes() - 1);
        int corrigiu = 0;
        for (int i = 0; i < raizes.numInstances(); i++) {
            Instance raiz = raizes.instance(i);
            String classeRealRaiz = raiz.stringValue(raiz.classIndex());
            int clusterRaiz = 0;
            try {
                clusterRaiz = clusterer.clusterInstance(raiz);
            } catch (Exception ex) {
                Logger.getLogger(Learn.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (!classeRealRaiz.equals(dict.get(clusterRaiz))) {
                corrigiu++;
                if (dict.containsValue(classeRealRaiz)) {
                    //System.out.println("TEM size: " + dic.size());
                } else {
                    dict.put(dict.size(), classeRealRaiz);
                }
                //corrige amostra
                raizes.instance(i).setClassValue(classeRealRaiz);
            }
        }

        List<Object> retorno = new ArrayList<>();
        retorno.add(dict);
        retorno.add(raizes);
        retorno.add(corrigiu);

        return retorno;
    }

    private List<Object> selecionaRaizesDaFronteira(Instances z2, int numFronteira, List<BeanAmostra> fronteirasTemp, List<Integer> assignments) {
        List<Integer> indicesConjZ2 = new ArrayList<>();
        Instances amostrasFronteira = new Instances(z2, numFronteira);
        for (int i = 0; i < numFronteira; i++) {
            BeanAmostra ba = fronteirasTemp.get(i);
            Instance sampleT = ba.getAmostra();
            amostrasFronteira.add(sampleT);
            indicesConjZ2.add(ba.getIndiceZ2());
        }

        Collections.sort(indicesConjZ2);
        Collections.reverse(indicesConjZ2);

        for (Integer indice : indicesConjZ2) {
            z2.delete(indice);
            assignments.remove(indice);
        }
        List<Object> retorno = new ArrayList<>();
        retorno.add(z2);
        retorno.add(assignments);
        retorno.add(amostrasFronteira);

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

}
