/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package learning;

import io.IOArff;
import io.IOText;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
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
            String classifiers, int kVizinhos, String ordenacao) {

        long init, end, diff;
        double time;
        
        new IOText().save(System.getProperty("user.dir").concat(File.separator),
                    "tempoOrdenacao", "0.0");
        
        new IOText().save(System.getProperty("user.dir").concat(File.separator),
                    "tempoSelecaoFronteira", "0.0");
        
        int numInstancias = z2.attribute(z2.numAttributes() - 1).numValues() * xNumClasses;
        int iteration = 0;

        List<Object> temp = criaCluster(numInstancias, z2);
        SimpleKMeans clusterer = (SimpleKMeans) temp.get(0);
        ClassificationViaClustering cvc = (ClassificationViaClustering) temp.get(1);
        //key = classe     value = cluster
        Map dicionario = (Map) temp.get(2);

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
                init = System.nanoTime();
                fronteirasTemp = selecionaAmostrasFronteira(z2, kVizinhos, cvc, ordenacao);
                end = System.nanoTime();
                diff = end - init;
                time = (diff / 1000000000.0);//tempo selecionar amostras de fronteira
                
                salvarFronteirasEmArquivo(fronteirasTemp, z2);
                
                new IOText().save(System.getProperty("user.dir").concat(File.separator),
                    "tempoSelecaoFronteira", String.valueOf(time));
            }

            init = System.nanoTime();
            int numCorrecoes = 0;
            int numFronteira = 0;
            boolean acabouAmostrasFronteira = false;
            if (iteration != 0) {
                numFronteira = clusterer.getNumClusters();
                if (numFronteira > fronteirasTemp.size()) {
                    numFronteira = fronteirasTemp.size();
                    acabouAmostrasFronteira = true;
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

            end = System.nanoTime();
            diff = end - init;
            time = (diff / 1000000000.0);//tempo de selecao

            int numClassesConhecidas = numClassesConhecidas(raizes);

            new IOArff().saveArffFile(raizes, "raizes" + iteration);

            classify(classifiers, iteration, raizes, z3, folds, time, numClassesConhecidas, numCorrecoes);

            if(acabouAmostrasFronteira){
                break;
            }

            iteration++;

        } while (true);
    }

    private List<Object> criaCluster(int size, Instances z2) {

        Instances z2SemClasse = removeAtributoClasse(z2);
        
        long init = System.nanoTime();
        
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
        
        long end = System.nanoTime();
        long diff = end - init;
        double time = (diff / 1000000000.0);//tempo agrupamento
        new IOText().save(System.getProperty("user.dir").concat(File.separator),
                    "tempoAgrupamento", String.valueOf(time));

        //dicionario init
        Enumeration classes = z2.attribute(z2.classIndex()).enumerateValues();
        Map<Integer, Double> dic = new HashMap();
        int contador = 0;
        do {
            //key = classe     value = cluster
            dic.put(Integer.valueOf((String) classes.nextElement()), (double) contador);
            contador++;
        } while (classes.hasMoreElements());

        //dicionario end
        
        List<Object> ret = new ArrayList<>();
        ret.add(clusterer);
        ret.add(cvc);
        ret.add(dic);

        return ret;
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
            List<BeanAmostra> vizinhosT, Instances z2, String ordenacao) {

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
        
        if(ordenacao.equals("Mm")){
            Collections.reverse(temp);
        }

        return temp;
    }

    private List<BeanAmostra> selecionaAmostrasFronteira(Instances z2,
            int kVizinhos, ClassificationViaClustering cvc, String ordenacao) {

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
        if(!ordenacao.contains("none")){
            long init = System.nanoTime();
            fronteirasTemp = ordenaAmostrasFronteira(amostrasT, vizinhosT, z2, ordenacao);
            long end = System.nanoTime();
            long diff = end - init;
            double time = (diff / 1000000000.0);//tempo de ordenação
            
            new IOText().save(System.getProperty("user.dir").concat(File.separator),
                    "tempoOrdenacao", String.valueOf(time));
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

    private void salvarFronteirasEmArquivo(List<BeanAmostra> fronteirasTemp, Instances z2) {
        Instances amostrasDaFronteira = new Instances(z2);
        amostrasDaFronteira.delete();
        
        for (BeanAmostra fronteira : fronteirasTemp) {
            amostrasDaFronteira.add(fronteira.getAmostra());
        }
        
        new IOArff().saveArffFile(amostrasDaFronteira, "amostrasDeFronteira");
        
    }

}
