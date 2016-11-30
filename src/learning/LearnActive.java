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
import weka.clusterers.AbstractClusterer;
import weka.clusterers.EM;
import weka.clusterers.FarthestFirst;
import weka.clusterers.SimpleKMeans;
import weka.core.DenseInstance;
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
    private Instances unlabeled;
    //private boolean isSupervisionado;

    private int indiceSelecaoPorLista = 0;

    public void active(Instances z2, Instances z3, int xNumClasses,
            int kVizinhos, String ordenacao, String classificador,
            String metodoSelecao, List<BeanAmostra> paramFronteiras,
            List<BeanAmostra> paramAmostrasT, List<BeanAmostra> paramVizinhosT,
            String tipoAgrupamento) {

        isSupervisionado = tipoClassificador(classificador);

        int numInstancias = z2.attribute(z2.numAttributes() - 1).numValues() * xNumClasses;
        int iteration = 0;

        Selecao selecao = null;

        AbstractClusterer clusterer = agrupamento(numInstancias, z2, tipoAgrupamento);

        //encontrar raizes
        Instances raizes = raizesProximasAoCentroide(z2, clusterer);

        z2 = atualizaZ2(z2, raizes);//remove as amostras raizes de z2
        
        
        //encontrar amostras de fronteira
        if (paramFronteiras == null) {
            //System.out.println("nao carregou externamente");
            selecionaAmostrasDeFronteira(clusterer, z2, kVizinhos);
        } else {
            //System.out.println("!!!!!!!!!!! carregou externamente!!!!!!!!!!!!!!!!!!!!");
            this.fronteiras = paramFronteiras;
            this.amostrasT = paramAmostrasT;
            this.vizinhosT = paramVizinhosT;
            salvarFronteirasEmArquivo(fronteiras, z2);
            new IOText().save(System.getProperty("user.dir").concat(File.separator),
                "tempoSelecaoFronteira", String.valueOf(0.0));
        }
        
        //ordena amostras de fronteira
        ordenacao(ordenacao, z2);
        
        z2 = atualizaZ2(z2, beanAmostra2Instances(z2, fronteiras));//remove as amostras de fronteira de z2

        new IOArff().saveArffFile(beanAmostra2Instances(z2, fronteiras), "fronteira");
        new IOArff().saveArffFile(beanAmostra2Instances(z2, amostrasT), "amostrasT");
        new IOArff().saveArffFile(beanAmostra2Instances(removeAtributoClasse(z2), vizinhosT), "vizinhosT");

        unlabeled = z2;
        unlabeled.setClassIndex(unlabeled.numAttributes() - 1);
        
        Instances amostrasSelecionadasUnlabeled = new Instances(unlabeled);
        amostrasSelecionadasUnlabeled.delete();
        
        if (isSupervisionado) {
//            try {
//                raizes = selecionaAmostras(metodoSelecao, selecao, clusterer.numberOfClusters()/2, raizes);
//            } catch (Exception ex) {
//                Logger.getLogger(LearnActive.class.getName()).log(Level.SEVERE, null, ex);
//            }
        } else {
            try{
                //seleciona do conjunto unlabeled
                amostrasSelecionadasUnlabeled = selecionaUnlabeled(numInstancias/2, amostrasSelecionadasUnlabeled);
                //seleciona do conjunto labeled
                //amostrasSelecionadasUnlabeled = selecionaAmostras(metodoSelecao, selecao, numInstancias/2, amostrasSelecionadasUnlabeled);
            } catch (Exception ex) {
                Logger.getLogger(LearnActive.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        new IOArff().saveArffFile(raizes, "raizes" + iteration);
        
        new IOArff().saveArffFile(amostrasSelecionadasUnlabeled, "unlabeled");
        System.out.println(raizes.numInstances() +"+"+ amostrasSelecionadasUnlabeled.numInstances() +"="+(raizes.numInstances() + amostrasSelecionadasUnlabeled.numInstances()));
        
        classifica(classificador, raizes, z3, amostrasSelecionadasUnlabeled);

        classesConhecidas = new HashSet<>();
        int numClassesConhecidas = classesConhecidas(raizes);
        List<String> outClassesConhecidas = new ArrayList<>();
        outClassesConhecidas.add(String.valueOf(numClassesConhecidas));
        outClassesConhecidas.add(classesConhecidas.toString());
        new IOText().save(System.getProperty("user.dir").concat(File.separator),
                "classesConhecidas", outClassesConhecidas);

        new IOText().save(System.getProperty("user.dir").concat(File.separator),
                "tempoSelecao", "0");

        salvaDados(classificador, iteration);

        iteration++;
        
        //loooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooop
        do {

            try {
                raizes = selecionaAmostras(metodoSelecao, selecao, clusterer.numberOfClusters(), raizes);
            } catch (Exception ex) {
                Logger.getLogger(LearnActive.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (isSupervisionado) {
//                try {
//                    raizes = selecionaAmostras(metodoSelecao, selecao, clusterer.numberOfClusters()/2, raizes);
//                } catch (Exception ex) {
//                    Logger.getLogger(LearnActive.class.getName()).log(Level.SEVERE, null, ex);
//                }
            } else {
                try{
                    //seleciona do conjunto unlabeled
                    amostrasSelecionadasUnlabeled = selecionaUnlabeled(numInstancias/2, amostrasSelecionadasUnlabeled);
                    //seleciona do conjunto labeled
                    //amostrasSelecionadasUnlabeled = selecionaAmostras(metodoSelecao, selecao, numInstancias/2, amostrasSelecionadasUnlabeled);
                } catch (Exception ex) {
                    Logger.getLogger(LearnActive.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            System.err.print("Salvando raízes em txt ... ");
            new IOArff().saveArffFile(raizes, "raizes" + iteration);
            System.err.println("feito");
            
            new IOArff().saveArffFile(amostrasSelecionadasUnlabeled, "unlabeled");
            System.out.println(raizes.numInstances() + "+" + amostrasSelecionadasUnlabeled.numInstances() + "=" + (raizes.numInstances() + amostrasSelecionadasUnlabeled.numInstances()));

            classifica(classificador, raizes, z3, amostrasSelecionadasUnlabeled);

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

    /**
     * Método responsável por criar um cluster, dado
     * <i>numClusteres</i>
     * e conjunto de dados <i>Z2</i>.
     *
     * @param numClusteres Número de clusteres que serão formados
     * @param z2 Conjunto de dados que será clusterizado.
     * @param tipoAgrupamento Qual o método de agrupamento a ser utilizado
     * @return Um cluster
     */
    private AbstractClusterer criaCluster(int numClusteres, Instances z2, String tipoAgrupamento) {
        Instances z2SemClasse = removeAtributoClasse(z2);

        AbstractClusterer clusterer = null;

        String options[] = null;
        switch (tipoAgrupamento) {
            case "SimpleKMeans":
                try {
                    options = weka.core.Utils.splitOptions("-N " + numClusteres + " -I 500 -S 10 -O");
                    clusterer = new SimpleKMeans();
                } catch (Exception ex) {
                    Logger.getLogger(LearnActive.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;

            case "EM":
                try {
                    options = weka.core.Utils.splitOptions("-N " + numClusteres + " -I 100 -S 100");
                    clusterer = new EM();

                } catch (Exception ex) {
                    Logger.getLogger(LearnActive.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
                
            case "FarthestFirst":
                try {
                    options = weka.core.Utils.splitOptions("-N " + numClusteres + " -S 1");
                    clusterer = new FarthestFirst();
                } catch (Exception ex) {
                    Logger.getLogger(LearnActive.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
        }

        try {
            clusterer.setOptions(options);
            clusterer.buildClusterer(z2SemClasse);
        } catch (Exception ex) {
            Logger.getLogger(LearnActive.class.getName()).log(Level.SEVERE, null, ex);
        }

        return clusterer;

    }

    private Instances raizesProximasAoCentroide(Instances z2, AbstractClusterer clusterer) {

        Instances centroids = null;

        if (clusterer instanceof SimpleKMeans) {
            SimpleKMeans kmeans = (SimpleKMeans) clusterer;
            centroids = kmeans.getClusterCentroids();

        } else if (clusterer instanceof EM) {
            EM em = (EM) clusterer;
            centroids = new Instances(z2);
            centroids.clear();

            /* This field holds the parameters of the normal distributions for numeric 
             attributes in the data. If you have only numeric attributes then this 
             does define the centroids. the indexes are #clusters, #attributes, 
             parameters. The later is a 3 element array: mean, standard deviation, 
             weight. */
            double[][][] centroidsTemp = em.getClusterModelsNumericAtts();

            for (double[][] centroidTemp : centroidsTemp) {
                double[] dblInst = new double[centroidTemp.length];
                for (int i = 0; i < centroidTemp.length; i++) {
                    double[] centroidMeans = centroidTemp[i];
                    dblInst[i] = centroidMeans[0];
                }
                Instance inst = new DenseInstance(1, dblInst);
                centroids.add(inst);
            }
        } else if (clusterer instanceof FarthestFirst) {
            FarthestFirst farth = (FarthestFirst) clusterer;
            centroids = farth.getClusterCentroids();

        }

//        for (int i = 0; i < centroids.numInstances(); i++) {
//            System.out.println(centroids.instance(i).toString());
//        }
//        
        //Instances centroids = clusterer.getClusterCentroids();
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

    /**
     * Método responsável por atualizar as amostras em <i>Z2</i>, ou seja,
     * remove as amostras de <i>Z2</i> que também estão em <i>raízes</i>.
     *
     * @param z2 Conjunto de dados que será atualizado (terá amostras retiradas)
     * @param raizes Conjunto de dados que não estará presente em <i>Z2</i>.
     * @return Conjunto de dados atualizado.
     */
    private Instances atualizaZ2(Instances z2, Instances raizes) {
        List<Integer> indicesApagar = new ArrayList<>();

        for (int i = 0; i < z2.numInstances(); i++) {
            for (int j = 0; j < raizes.numInstances(); j++) {
                if (z2.instance(i).toString().equals(raizes.instance(j).toString())) {
                    indicesApagar.add(i);
                }
            }
        }
        Set<Integer> uniques = new HashSet<>();
        uniques.addAll(indicesApagar);

        indicesApagar.clear();

        for (Integer unique : uniques) {
            indicesApagar.add(unique);
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

    /**
     * Método responsável por encontrar as amostras de fronteira entre clusters.
     * Esse método utiliza o próprio clusterer para encontrar as amostras de
     * fronteira.
     *
     * @param clusterer Cluster previamente construído, do tipo
     * <i>SimpleKMeans</i>
     * @param z2 Conjunto de dados que será verificado, onde as amostras de
     * fronteira serão extraídas
     * @param kVizinhos Número de vizinhos que serão verificados
     */
    private void fronteira(AbstractClusterer clusterer, Instances z2, int kVizinhos) {
        fronteiras = new ArrayList<>();
        amostrasT = new ArrayList<>();
        vizinhosT = new ArrayList<>();

        Instances z2SemClasse = removeAtributoClasse(z2);

        for (int i = 0; i < z2SemClasse.numInstances(); i++) {
            Instance t = z2SemClasse.instance(i);
            int clusterT = 0;

            try {
                clusterT = clusterer.clusterInstance(t);
            } catch (Exception ex) {
                Logger.getLogger(LearnActive.class.getName()).log(Level.SEVERE, null, ex);
            }
            Instances vizinhos = null;
            
            EuclideanDistance df = new EuclideanDistance(z2SemClasse);
            df.setDontNormalize(true);
            KDTree tree = new KDTree();
            try {
                tree.setInstances(z2SemClasse);
                tree.setDistanceFunction(df);
                vizinhos = tree.kNearestNeighbours(t, kVizinhos);
            } catch (Exception ex) {
                Logger.getLogger(LearnActive.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            for (int j = 0; j < vizinhos.numInstances(); j++) {
                int clusterV = 0;
                try {
                    clusterV = clusterer.clusterInstance(vizinhos.instance(j));
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

    /**
     * Método responsável por encontrar as amostras de fronteira entre clusters.
     * Esse método utiliza o classificador para encontrar as amostras de
     * fronteira.
     *
     * @param z2 Conjunto de dados que será verificado, onde as amostras de
     * fronteira serão extraídas
     * @param kVizinhos Número de vizinhos que serão verificados
     */
    private void fronteira(Instances z2, int kVizinhos) {
        fronteiras = new ArrayList<>();
        amostrasT = new ArrayList<>();
        vizinhosT = new ArrayList<>();

        for (int i = 0; i < z2.numInstances(); i++) {
            Instance t = z2.instance(i);
            double clusterT = 0;
            try {
                clusterT = classifier.classifyInstance(t);
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
                    clusterV = classifier.classifyInstance(vizinhos.instance(j));
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

    private Instances beanAmostra2Instances(Instances model, List<BeanAmostra> amostras) {
        Instances ret = new Instances(model);
        ret.clear();

        for (BeanAmostra amostra : amostras) {
            ret.add(amostra.getAmostra());
        }
        return ret;
    }

    private AbstractClusterer agrupamento(int numInstancias, Instances z2, String tipoAgrupamento) {
        System.err.print("Realizando agrupamento ... ");
        long init = System.nanoTime();
        AbstractClusterer clusterer = criaCluster(numInstancias, z2, tipoAgrupamento);
        long end = System.nanoTime();
        long diff = end - init;
        double time = (diff / 1000000000.0);//tempo agrupamento
        new IOText().save(System.getProperty("user.dir").concat(File.separator),
                "tempoAgrupamento", String.valueOf(time));
        System.err.println("feito");
        return clusterer;
    }

    /**
     * Método responsável por invocar o método para selecionar as amostras de
     * fronteira. Esse método utiliza o classificador para encontrar as amostras
     * de fronteira.
     *
     * @param z2 Conjunto de dados que será verificado, onde as amostras de
     * fronteira serão extraídas
     * @param kVizinhos Número de vizinhos que serão verificados
     */
    private void selecionaAmostrasDeFronteira(Instances z2, int kVizinhos) {
        System.err.print("Procurando amostras de fronteira ... ");
        long init = System.nanoTime();

        fronteira(z2, kVizinhos);//seleciona as amostras de fronteira
        long end = System.nanoTime();
        long diff = end - init;
        double time = (diff / 1000000000.0);//tempo selecionar amostras de fronteira
        salvarFronteirasEmArquivo(fronteiras, z2);
        new IOText().save(System.getProperty("user.dir").concat(File.separator),
                "tempoSelecaoFronteira", String.valueOf(time));
        System.err.println("feito");
    }

    /**
     * Método responsável por invocar o método para selecionar as amostras de
     * fronteira. Esse método utiliza o próprio clusterer para encontrar as
     * amostras de fronteira.
     *
     * @param clusterer Cluster previamente construído, do tipo
     * <i>SimpleKMeans</i>
     * @param z2 Conjunto de dados que será verificado, onde as amostras de
     * fronteira serão extraídas
     * @param kVizinhos Número de vizinhos que serão verificados
     */
    private void selecionaAmostrasDeFronteira(AbstractClusterer clusterer, Instances z2, int kVizinhos) {
        System.err.print("Procurando amostras de fronteira ... ");
        long init = System.nanoTime();

        fronteira(clusterer, z2, kVizinhos);//seleciona as amostras de fronteira
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

    private Instances selecionaAmostras(String metodoSelecao, Selecao selecao, int numAmostras, Instances raizes) {
        long init = System.nanoTime();
        switch (metodoSelecao) {
            case "lista":
                if (selecao == null) {
                    selecao = new SelecaoListas(fronteiras);
                }
                raizes = selecao.seleciona(numAmostras, raizes);
                isFronteiraEmpty = selecao.isEmpty();
                break;
            case "ordem":
                if (selecao == null) {
                    selecao = new SelecaoOrdem(fronteiras);
                }
                raizes = selecao.seleciona(numAmostras, raizes);
                isFronteiraEmpty = selecao.isEmpty();
                break;
        }
        novasAmostrasDeFronteiraSelcionadas = selecao.getAmostrasSelecionadas();
        long end = System.nanoTime();
        long diff = end - init;
        double time = (diff / 1000000000.0);//tempo de seleção
        new IOText().save(System.getProperty("user.dir").concat(File.separator),
                "tempoSelecao", String.valueOf(time));

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
    private Instances selecionaUnlabeled(int nAmostras, Instances amostrasUnlabeled) {

        if (unlabeled.numInstances() == 0) {
            return amostrasUnlabeled;
        }

        if (nAmostras > unlabeled.numInstances()) {
            nAmostras = unlabeled.numInstances() - 1;
        }

        for (int i = 0; i < nAmostras; i++) {
            amostrasUnlabeled.add(unlabeled.instance(0));
            unlabeled.remove(0);
        }

        return amostrasUnlabeled;
    }

}
