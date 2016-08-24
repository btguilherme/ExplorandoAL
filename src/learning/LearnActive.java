/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package learning;

import arff2opf.Arff2opf;
import io.IOArff;
import io.IOText;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import selecao.Selecao;
import selecao.SelecaoListas;
import selecao.SelecaoOrdem;
import utils.RunCommand;
import weka.classifiers.Classifier;
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
public class LearnActive extends Learn {

    private List<BeanAmostra> fronteiras;
    private List<BeanAmostra> amostrasT;
    private List<BeanAmostra> vizinhosT;
    private boolean isFronteiraEmpty;
    private Instances novasAmostrasDeFronteiraSelcionadas;

    private int indiceSelecaoPorLista = 0;

    public void active(Instances z2, Instances z3, int xNumClasses,
            int kVizinhos, String ordenacao, String classificador,
            String metodoSelecao) {

        int numInstancias = z2.attribute(z2.numAttributes() - 1).numValues() * xNumClasses;
        int iteration = 0;

        SimpleKMeans clusterer = agrupamento(numInstancias, z2);

        Instances raizes = raizesProximasAoCentroide(z2, clusterer);

        System.err.print("Salvando raízes em txt ... ");
        new IOArff().saveArffFile(raizes, "raizes" + iteration);
        System.err.println("feito");

        z2 = atualizaZ2(z2, raizes);//remove as amostras raizes de z2

        classifica(classificador, raizes, z3, null);

        selecionaAmostrasDeFronteira(z2, kVizinhos);

        Instances instFronteiras = beanAmostra2Instances(z2);

        z2 = atualizaZ2(z2, instFronteiras);

        Instances unlabeled = z2;
        new IOArff().saveArffFile(unlabeled, "unlabeled");

        classifica(classificador, raizes, z3, unlabeled);

        classesConhecidas = new HashSet<>();
        int numClassesConhecidas = classesConhecidas(raizes);
        List<String> outClassesConhecidas = new ArrayList<>();
        outClassesConhecidas.add(String.valueOf(numClassesConhecidas));
        outClassesConhecidas.add(classesConhecidas.toString());
        new IOText().save(System.getProperty("user.dir").concat(File.separator),
                "classesConhecidas", outClassesConhecidas);

        //ordena amostras de fronteira
        ordenacao(ordenacao, z2);

        salvaDados(classificador, iteration);

        iteration++;

        //loooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooop
        Selecao selecao = null;
        do {

            raizes = selecionaAmostras(metodoSelecao, selecao, clusterer, raizes);

            System.err.print("Salvando raízes em txt ... ");
            new IOArff().saveArffFile(raizes, "raizes" + iteration);
            System.err.println("feito");

            classifica(classificador, raizes, z3, unlabeled);

            numClassesConhecidas = classesConhecidas(novasAmostrasDeFronteiraSelcionadas);
            outClassesConhecidas = new ArrayList<>();
            outClassesConhecidas.add(String.valueOf(numClassesConhecidas));
            outClassesConhecidas.add(classesConhecidas.toString());
            new IOText().save(System.getProperty("user.dir").concat(File.separator),
                    "classesConhecidas", outClassesConhecidas);

            //salva iteracao
            salvaDados(classificador, iteration);

            iteration++;

        } while (!isFronteiraEmpty);

    }

    private SimpleKMeans criaCluster(int numClusteres, Instances z2) {
        Instances z2SemClasse = removeAtributoClasse(z2);
        SimpleKMeans clusterer = new SimpleKMeans();
        clusterer.setSeed(10);
        clusterer.setPreserveInstancesOrder(true);
        try {
            clusterer.setNumClusters(numClusteres / 2);
            clusterer.setMaxIterations(500);
            clusterer.buildClusterer(z2SemClasse);
        } catch (Exception ex) {
            Logger.getLogger(LearnActive.class.getName()).log(Level.SEVERE, null, ex);
        }
        return clusterer;
    }

    private Instances raizesProximasAoCentroide(Instances z2, SimpleKMeans clusterer) {
        Instances centroids = clusterer.getClusterCentroids();
        Instances raizes = new Instances(z2, centroids.numInstances());
        for (int i = 0; i < centroids.numInstances(); i++) {
            Instance inst = centroids.instance(i);
            KDTree tree = new KDTree();
            try {
                tree.setInstances(z2);
                raizes.add(tree.kNearestNeighbours(inst, 1).firstInstance());
            } catch (Exception ex) {
                Logger.getLogger(LearnActive.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return raizes;
    }

    private Instances atualizaZ2(Instances z2, Instances raizes) {
        //Instances dataClusterer = removeAtributoClasse(z2);
        List<Integer> indicesApagar = new ArrayList<>();
        for (int i = 0; i < z2.numInstances(); i++) {
            for (int j = 0; j < raizes.numInstances(); j++) {
                if (z2.instance(i).toString().contains(raizes.instance(j).toString())) {
                    indicesApagar.add(i);
                }
            }
        }
        Collections.sort(indicesApagar);
        Collections.reverse(indicesApagar);
        for (Integer indice : indicesApagar) {
            z2.delete(indice);
        }

        return z2;
    }

    private Instances removeAtributoClasse(Instances z2) {
        Instances dataClusterer = null;
        weka.filters.unsupervised.attribute.Remove filter = new weka.filters.unsupervised.attribute.Remove();
        filter.setAttributeIndices("" + (z2.classIndex() + 1));

        try {
            filter.setInputFormat(z2);
            dataClusterer = Filter.useFilter(z2, filter);
        } catch (Exception ex) {
            Logger.getLogger(LearnActive.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dataClusterer;
    }

    private void salvarFronteirasEmArquivo(List<BeanAmostra> fronteirasTemp, Instances z2) {
        Instances amostrasDaFronteira = new Instances(z2);
        amostrasDaFronteira.delete();
        for (BeanAmostra fronteira : fronteirasTemp) {
            amostrasDaFronteira.add(fronteira.getAmostra());
        }
        new IOArff().saveArffFile(amostrasDaFronteira, "amostrasDeFronteira");
    }

    protected void arff2ascii(String path, String opt) {
        //arrf2opf ascii
        String[] args = {path, path + opt};

        Arff2opf a2o1 = new Arff2opf();
        a2o1.main(args);
        while (a2o1.isFinished() == false) {/**/

        }
    }

    protected void ascii2dat(String path, String dat) {
        String comando = "txt2opf " + path + " " + dat;
        RunCommand.runCommand(comando);
    }

    private double getClassOPF(Instance t, Instances z2) {
        Instances temp = new Instances(z2);
        temp.delete();
        temp.add(t);
        new IOArff().saveArffFile(temp, "temp");
        arff2ascii("temp.arff", "");
        ascii2dat("temp.arff.opf", "temp.dat");
        String command = "opf_classify temp.dat";
        RunCommand.runCommand(command);
        IOText io = new IOText();
        List<String> tempOut = io.open(System.getProperty("user.dir")
                .concat(File.separator).concat("temp.dat.out"));

        return Double.valueOf(tempOut.get(0));
    }



    private void fronteira(Instances z2, int kVizinhos, Classifier classificador) {
        fronteiras = new ArrayList<>();
        amostrasT = new ArrayList<>();
        vizinhosT = new ArrayList<>();

        for (int i = 0; i < z2.numInstances(); i++) {
            Instance t = z2.instance(i);
            double clusterT = 0;
            try {
                clusterT = classificador.classifyInstance(t);
            } catch (Exception ex) {
                Logger.getLogger(LearnActive.class.getName()).log(Level.SEVERE, null, ex);
            }
            Instances vizinhos = null;
            KDTree tree = new KDTree();
            EuclideanDistance df = new EuclideanDistance(z2);
            df.setDontNormalize(true);
            try {
                tree.setInstances(z2);
                tree.setDistanceFunction(df);
                vizinhos = tree.kNearestNeighbours(t, kVizinhos);
                vizinhos.setClassIndex(vizinhos.numAttributes() - 1);
            } catch (Exception ex) {
                Logger.getLogger(LearnActive.class.getName()).log(Level.SEVERE, null, ex);
            }
            for (int j = 0; j < vizinhos.numInstances(); j++) {
                double clusterV = 0;
                try {
                    clusterV = classificador.classifyInstance(vizinhos.instance(j));
                } catch (Exception ex) {
                    Logger.getLogger(LearnActive.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (clusterT != clusterV) {
                    BeanAmostra amostraT = new BeanAmostra(z2.instance(i), clusterT, i);
                    BeanAmostra vizinhoT = new BeanAmostra(vizinhos.instance(j), 0, j);
                    amostrasT.add(amostraT);
                    vizinhosT.add(vizinhoT);
                    fronteiras.add(amostraT);
                    break;
                }
            }
        }
        isFronteiraEmpty = fronteiras.isEmpty();
    }

    private void ordenar(String ordenacao, Instances z2) {
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

        if (ordenacao.equals("Mm")) {
            Collections.reverse(temp);
        }

        fronteiras = temp;
    }

    private Instances beanAmostra2Instances(Instances z2) {
        Instances ret = new Instances(z2);
        ret.clear();

        for (BeanAmostra fronteira : fronteiras) {
            ret.add(fronteira.getAmostra());
        }
        return ret;
    }

    private SimpleKMeans agrupamento(int numInstancias, Instances z2) {
        System.err.print("Realizando agrupamento ... ");
        long init = System.nanoTime();
        SimpleKMeans clusterer = criaCluster(numInstancias, z2);
        long end = System.nanoTime();
        long diff = end - init;
        double time = (diff / 1000000000.0);//tempo agrupamento
        new IOText().save(System.getProperty("user.dir").concat(File.separator),
                "tempoAgrupamento", String.valueOf(time));
        System.err.println("feito");
        return clusterer;
    }

    private void selecionaAmostrasDeFronteira(Instances z2, int kVizinhos) {
        System.err.print("Procurando amostras de fronteira ... ");
        long init = System.nanoTime();

        fronteira(z2, kVizinhos, classifier);//seleciona as amostras de fronteira
        long end = System.nanoTime();
        long diff = end - init;
        double time = (diff / 1000000000.0);//tempo selecionar amostras de fronteira
        salvarFronteirasEmArquivo(fronteiras, z2);
        new IOText().save(System.getProperty("user.dir").concat(File.separator),
                "tempoSelecaoFronteira", String.valueOf(time));
        System.err.println("feito");
    }

    private void ordenacao(String ordenacao, Instances z2) {
        System.err.print("Ordenando amostras de fronteira ... ");
        long init = System.nanoTime();
        if (!ordenacao.equals("none")) {
            ordenar(ordenacao, z2);
        }
        long end = System.nanoTime();
        long diff = end - init;
        double time = (diff / 1000000000.0);//tempo de ordenação
        if (ordenacao.equals("none")) {
            time = 0.0;
        }
        new IOText().save(System.getProperty("user.dir").concat(File.separator),
                "tempoOrdenacao", String.valueOf(time));
        System.err.println("feito");
    }

    private Instances selecionaAmostras(String metodoSelecao, Selecao selecao, SimpleKMeans clusterer, Instances raizes) {
        switch (metodoSelecao) {
            case "lista":
                if (selecao == null) {
                    selecao = new SelecaoListas(fronteiras);
                }
                raizes = selecao.seleciona(clusterer, raizes);
                isFronteiraEmpty = selecao.isEmpty();
                break;
            case "ordem":
                if (selecao == null) {
                    selecao = new SelecaoOrdem(fronteiras);
                }
                raizes = selecao.seleciona(clusterer, raizes);
                isFronteiraEmpty = selecao.isEmpty();
                break;
        }
        novasAmostrasDeFronteiraSelcionadas = selecao.getAmostrasSelecionadas();
        return raizes;
    }

//    private Instances selecionaAmostrasDaLista(SimpleKMeans clusterer,
//            Instances raizes, Selecao selecao) {
//        
//        System.err.print("Selecionando amostras de fronteira ... ");
//        long init = System.nanoTime();
//
//        raizes = selecao.seleciona(clusterer, raizes);
//
//        long end = System.nanoTime();
//        long diff = end - init;
//        double time = (diff / 1000000000.0);
//        new IOText().save(System.getProperty("user.dir").concat(File.separator),
//                "tempoSelecao", String.valueOf(time));
//        System.err.println("feito");
//
//        return raizes;
//    }
//    private Instances selecionaFronteiraMetodoLista(SimpleKMeans clusterer, Instances raizes) {
//
//        int numFronteira = clusterer.getNumClusters();
//        novasAmostrasDeFronteiraSelcionadas = new Instances(raizes);
//        novasAmostrasDeFronteiraSelcionadas.delete();
//
//        if (numFronteira > fronteiras.size()) {
//            numFronteira = fronteiras.size() - 1;
//            isFronteiraEmpty = true;
//        }
//
//        HashMap<String, Instances> listas = new HashMap<>();
//        Enumeration<Object> classes = raizes.attribute("class").enumerateValues();
//
//        //cria a estrutura da lista
//        while (classes.hasMoreElements()) {
//            Instances is = new Instances(raizes);
//            is.delete();
//            listas.put((String) classes.nextElement(), is);
//        }
//
//        //popula a estrutura com as amostras de fronteira, separadas 
//        //por cada classe
//        for (int j = 0; j < fronteiras.size(); j++) {
//            Instance amostraTemp = fronteiras.get(j).getAmostra();
//            String classeRealAmostraTemp = amostraTemp.toString(amostraTemp.numAttributes() - 1);
//            Instances temp = listas.get(classeRealAmostraTemp);
//            temp.add(amostraTemp);
//            listas.put(classeRealAmostraTemp, temp);
//        }
//
//        Object[] keys = listas.keySet().toArray();
//        
//        //
//        for (int i = 0; i < keys.length; i++) {
//            String key = (String)keys[i];
//            System.out.println(key);
//            
//            Instances values = listas.get(key);
//            
//            for (int j = 0; j < values.numInstances(); j++) {
//                System.out.println("\t"+values.instance(j));
//            }
//            System.out.println("");
//            
//        }
//        //
//        
//        
//        for (int i = 0; i < numFronteira; i++) {
//
//            //controle da variavel 'index' (evitar null pointer)
//            if (listas.get((String) keys[indiceSelecaoPorLista]).numInstances() == 0) {
//                do {
//                    if (indiceSelecaoPorLista == keys.length - 1) {
//                        indiceSelecaoPorLista = 0;
//                    } else {
//                        indiceSelecaoPorLista++;
//                    }
//                } while (listas.get((String) keys[indiceSelecaoPorLista]).numInstances() == 0);
//            }
//
//            Instances aux = listas.get((String) keys[indiceSelecaoPorLista]);
//            BeanAmostra amostra = new BeanAmostra(aux.instance(0), 0.0, 0);
//            aux.delete(0);
//            listas.put((String) keys[indiceSelecaoPorLista], aux);
//            raizes.add(amostra.getAmostra());
//            
//            //
//            System.out.println("Amostra selecionada: "+amostra.getAmostra().toString());
//            //
//            novasAmostrasDeFronteiraSelcionadas.add(amostra.getAmostra());
//
//            if (indiceSelecaoPorLista == keys.length - 1) {
//                indiceSelecaoPorLista = 0;
//            } else {
//                indiceSelecaoPorLista++;
//            }
//        }
//
//        for (int i = 0; i < novasAmostrasDeFronteiraSelcionadas.size(); i++) {
//            String novaAmostra = novasAmostrasDeFronteiraSelcionadas.get(i).toString();
//            for (int j = fronteiras.size() - 1; j >= 0; j--) {
//                String amostraFronteira = fronteiras.get(j).getAmostra().toString();
//                if(novaAmostra.equals(amostraFronteira)){
//                    fronteiras.remove(j);
//                }
//                
//            }
//        }
//        
//        return raizes;
//    }
    
    //    private int verificaRaizesClassificadasErradas(Instances amostras) {
//        int contador = 0;
//        try {
//            for (int i = 0; i < amostras.numInstances(); i++) {
//                String pred = String.valueOf(getClassOPF(amostras.instance(i), amostras));
//                String actual = amostras.classAttribute().value((int) amostras.instance(i).classValue());
//
//                if (!actual.equals(pred)) {
//                    contador++;
//                }
//            }
//        } catch (Exception ex) {
//            Logger.getLogger(LearnActive.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return contador;
//    }
 
    //    private void fronteira(Instances z2, int kVizinhos) {
//        fronteiras = new ArrayList<>();
//        amostrasT = new ArrayList<>();
//        vizinhosT = new ArrayList<>();
//
//        for (int i = 0; i < z2.numInstances(); i++) {
//
//            Instance t = z2.instance(i);
//
//            double clusterT = getClassOPF(t, z2);
//
//            Instances vizinhos = null;
//            KDTree tree = new KDTree();
//            EuclideanDistance df = new EuclideanDistance(z2);
//            df.setDontNormalize(true);
//            try {
//                tree.setInstances(z2);
//                tree.setDistanceFunction(df);
//                vizinhos = tree.kNearestNeighbours(t, kVizinhos);
//                vizinhos.setClassIndex(vizinhos.numAttributes() - 1);
//            } catch (Exception ex) {
//                Logger.getLogger(LearnActive.class.getName()).log(Level.SEVERE, null, ex);
//            }
//
//            for (int j = 0; j < vizinhos.numInstances(); j++) {
//                double clusterV = getClassOPF(vizinhos.instance(j), z2);
//                if (clusterT != clusterV) {
//                    BeanAmostra amostraT = new BeanAmostra(z2.instance(i), clusterT, i);
//                    BeanAmostra vizinhoT = new BeanAmostra(vizinhos.instance(j), 0, j);
//                    amostrasT.add(amostraT);
//                    vizinhosT.add(vizinhoT);
//                    fronteiras.add(amostraT);
//                    break;
//                }
//            }
//        }
//        isFronteiraEmpty = fronteiras.isEmpty();
//    }

}
