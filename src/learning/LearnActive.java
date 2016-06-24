/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package learning;

import io.IOArff;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.Main;
import weka.classifiers.meta.ClassificationViaClustering;
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

    public void active(Instances z2, Instances z3, int folds, int xNumClasses,
            String classifiers, int kVizinhos) {

        IOArff ioArff = new IOArff();

        int numInstancias = z2.attribute(z2.numAttributes() - 1).numValues() * xNumClasses;
        int iteration = 0;

        List<Object> temp = criaCluster(numInstancias, z2);
        SimpleKMeans clusterer = (SimpleKMeans) temp.get(0);
        ClassificationViaClustering cvc = (ClassificationViaClustering) temp.get(1);
        int classesConhecidas = (int) temp.get(2);
        //key = classe     value = cluster
        Map dicionario = (Map) temp.get(3);

        Instances centroids = clusterer.getClusterCentroids();
        Instances raizes = raizesProximasAoCentroide(centroids, z2);

        //remove as instancias raizes de z2 (remove duplicatas)
        //mantem as classes
        temp = atualizaZ2(z2, raizes);
        z2 = (Instances) temp.get(0);

        List<BeanAmostra> fronteirasTemp = new ArrayList<>();

        int numAmostrasZ2 = z2.numInstances();

        Instances amostrasCorrigidas = new Instances(z2);
        amostrasCorrigidas.delete();

        do {
            /*
             primeira iteracao (iteration == 0) é utilizada apenas as amostras raizes
             na segunda iteracao (iteration == 1) as amostras de fronteira são selecionadas.
             para demais iteracoes, amostras são selecionadas do conjunto de amostras de fronteira
             */
            if (iteration == 1) {
                fronteirasTemp = selecionaAmostrasFronteira(z2, clusterer, kVizinhos, cvc);
            }

            long init = System.nanoTime();

            int numCorrecoes = 0;
            if (iteration != 0) {
                int numFronteira = clusterer.getNumClusters();

                if (numFronteira > fronteirasTemp.size()) {
                    numFronteira = fronteirasTemp.size();
                }

                numAmostrasZ2 = numAmostrasZ2 - numFronteira;

                temp = selecionaAmostrasDaFronteira(z2, numFronteira, fronteirasTemp);
                z2 = (Instances) temp.get(0);
                Instances amostrasFronteira = (Instances) temp.get(1);
                fronteirasTemp = (List<BeanAmostra>) temp.get(2);

                temp = corrigeRotulos(amostrasFronteira, amostrasCorrigidas, cvc, dicionario);
                amostrasCorrigidas = (Instances) temp.get(0);

                if (amostrasFronteira.numInstances() != 0) {
                    for (int i = 0; i < amostrasFronteira.numInstances(); i++) {
                        raizes.add(amostrasFronteira.instance(i));
                    }
                }
                numCorrecoes = (int) temp.get(1);
            }

            long end = System.nanoTime();
            long diff = end - init;
            double time = (diff / 1000000000.0);//tempo de selecao

            int numClassesConhecidas;
            if (iteration == 0) {
                numClassesConhecidas = classesConhecidas;
            } else {
                numClassesConhecidas = numClassesConhecidas(raizes);
            }

            ioArff.saveArffFile(raizes, "raizes" + iteration);

            classify(classifiers, iteration, raizes, z3, folds, time, numClassesConhecidas, numCorrecoes);

            //if (iteration != 0 && (fronteirasTemp.size() - numInstancias) < numInstancias) {
            if (iteration != 0 && fronteirasTemp.size() < numInstancias) {
                break;
            }

            iteration++;

        } while (true);
    }

    private List<Object> criaCluster(int size, Instances z2) {

        Instances z2SemClasse = removeAtributoClasse(z2);

        SimpleKMeans clusterer = new SimpleKMeans();
        clusterer.setSeed(10);
        clusterer.setPreserveInstancesOrder(true);
        try {
            clusterer.setNumClusters(size / 2);
            clusterer.setMaxIterations(500);
            clusterer.buildClusterer(z2SemClasse);
        } catch (Exception ex) {
            Logger.getLogger(Learn.class.getName()).log(Level.SEVERE, null, ex);
        }

        ClassificationViaClustering cvc = new ClassificationViaClustering();
        cvc.setClusterer(clusterer);

        try {
            cvc.buildClassifier(z2);
        } catch (Exception ex) {
            Logger.getLogger(LearnActive.class.getName()).log(Level.SEVERE, null, ex);
        }

        //inicio classes conhecidas (com o clustering apenas)
        String classesClusters = cvc.toString().split("Classes to clusters mapping:\n")[1];
        String[] linhas = classesClusters.split("\n");
        int contClassesConhecidas = 0;
        for (int i = 0; i < linhas.length; i++) {
            if (linhas[i].split(":")[1].contains("no cluster")) {
                //
            } else {
                contClassesConhecidas++;
            }
        }
        //fim classes conhecidas (com o clustering apenas)

        Enumeration classes = z2.attribute(z2.classIndex()).enumerateValues();
        Map<Integer, Double> dic = new HashMap();
        int contador = 0;
        do {
            //key = classe     value = cluster
            dic.put(Integer.valueOf((String) classes.nextElement()), (double) contador);
            contador++;
        } while (classes.hasMoreElements());

        List<Object> ret = new ArrayList<>();
        ret.add(clusterer);
        ret.add(cvc);
        ret.add(contClassesConhecidas);//int
        ret.add(dic);

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

    private List<Object> corrigeRotulos(Instances raizes, Instances amostrasCorrigidas,
            ClassificationViaClustering cvc, Map dicionario) {

        int corrigiu = 0;

        for (int i = 0; i < raizes.numInstances(); i++) {
            Instance raiz = raizes.instance(i);
            double clusterRaizDado = 0;

            try {
                clusterRaizDado = cvc.classifyInstance(raiz);
            } catch (Exception ex) {
                Logger.getLogger(Learn.class.getName()).log(Level.SEVERE, null, ex);
            }

            int classeReal = Integer.valueOf(raiz.stringValue(raiz.classIndex()));

            double clusterRaizOriginal;
            //key = classe     value = cluster
            if (dicionario.containsKey(classeReal)) {//devolver o cluster
                clusterRaizOriginal = (double) dicionario.get(classeReal);
                if (clusterRaizDado != clusterRaizOriginal) {
                    if (amostrasCorrigidas.numInstances() == 0) {
                        amostrasCorrigidas.add(raiz);
                        corrigiu++;
                    } else {
                        boolean amostraJaCorrigida = false;
                        for (int j = 0; j < amostrasCorrigidas.numInstances(); j++) {
                            String amostraCorrigida = amostrasCorrigidas.instance(j).toString();
                            String amostraACorrigir = raiz.toString();
                            if (amostraCorrigida.contains(amostraACorrigir)) {
                                amostraJaCorrigida = true;
                            }
                        }
                        if (!amostraJaCorrigida) {
                            amostrasCorrigidas.add(raiz);
                            corrigiu++;
                        }
                    }
                }
            }
        }

        List<Object> retorno = new ArrayList<>();
        retorno.add(amostrasCorrigidas);
        retorno.add(corrigiu);

        return retorno;
    }

    private List<Object> selecionaAmostrasDaFronteira(Instances z2, int numFronteira,
            List<BeanAmostra> fronteirasTemp) {

        List<Integer> indicesConjZ2 = new ArrayList<>();
        Instances amostrasFronteira = new Instances(z2, numFronteira);
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

        return retorno;
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

    private List<BeanAmostra> selecionaAmostrasFronteira(Instances z2,
            SimpleKMeans clusterer, int kVizinhos, ClassificationViaClustering cvc) {

        List<BeanAmostra> fronteirasTemp = new ArrayList<>();
        List<BeanAmostra> amostrasT = new ArrayList<>();
        List<BeanAmostra> vizinhosT = new ArrayList<>();

        for (int i = 0; i < z2.numInstances(); i++) {
            Instance t = z2.instance(i);

            double clusterT = 0;
            try {
                clusterT = cvc.classifyInstance(t);
            } catch (Exception ex) {
                Logger.getLogger(LearnActive.class.getName()).log(Level.SEVERE, null, ex);
            }

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
                vizinhos.setClassIndex(vizinhos.numAttributes() - 1);
            } catch (Exception ex) {
                Logger.getLogger(Learn.class.getName()).log(Level.SEVERE, null, ex);
            }

            for (int j = 0; j < vizinhos.numInstances(); j++) {

                double clusterV = 0;
                try {
                    clusterV = cvc.classifyInstance(vizinhos.instance(j));
                } catch (Exception ex) {
                    Logger.getLogger(LearnActive.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (clusterT != clusterV) {//amostra de fronteira

                    BeanAmostra amostraT = new BeanAmostra(z2.instance(i), clusterT, i);
                    BeanAmostra vizinhoT = new BeanAmostra(vizinhos.instance(j), 0, j);

                    amostrasT.add(amostraT);
                    vizinhosT.add(vizinhoT);
                    
                    fronteirasTemp.add(amostraT);

                    break;
                }
            }
        }

        //ordenar da menor distancia para a maior
        if(Main.ORDENA_AMOSTRAS){
            fronteirasTemp = ordenaAmostrasFronteira(amostrasT, vizinhosT, z2);
        }

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
