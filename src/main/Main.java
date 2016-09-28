/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import io.IOArff;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import learning.BeanAmostra;
import learning.LearnActive;
import learning.LearnRandom;
import splitter.Splitter;
import weka.core.Instances;

/**
 *
 * @author guilherme
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        FileInputStream file
                = new FileInputStream(System.getProperty("user.dir").concat(File.separator)
                        + "/src/properties/propriedades.properties");
        props.load(file);

        int XNUMCLASSES = Integer.valueOf(props.getProperty("prop.xnumclasses"));
        String OPC_APRENDIZADO = props.getProperty("prop.aprendizado");
        int MAX_EXECS = Integer.valueOf(props.getProperty("prop.execucoes"));
        boolean INPUT_MANUAL = Boolean.valueOf(props.getProperty("prop.inputManual"));
        String ORDENACAO = props.getProperty("prop.ordenacao");
        String CLASSIFICADOR = props.getProperty("prop.classificador");
        String METODOSELECAO = props.getProperty("prop.selecaoFronteira");

        Movimentacao mov = new Movimentacao();
        IOArff io = new IOArff();

        Instances z2 = null, z3 = null;
        List<BeanAmostra> fronteiras = null, amostrasT = null, vizinhosT = null;

        if (INPUT_MANUAL == true) {
            List<Object> temp
                    = inputManual(
                            props.getProperty("prop.inputManual.treino"),
                            props.getProperty("prop.inputManual.teste"),
                            props.getProperty("prop.inputManual.fronteira"),
                            props.getProperty("prop.inputManual.amostrasT"),
                            props.getProperty("prop.inputManual.vizinhosT")
                    );
            z2 = (Instances) temp.get(0);
            z3 = (Instances) temp.get(1);
            fronteiras = (List<BeanAmostra>) temp.get(2);
            amostrasT = (List<BeanAmostra>) temp.get(3);
            vizinhosT = (List<BeanAmostra>) temp.get(4);
        }

        String[] aprendizado = OPC_APRENDIZADO.split(" ");
        String[] ordenacao = ORDENACAO.split(" ");
        String[] selecao = METODOSELECAO.split(" ");
        String[] classificadores = CLASSIFICADOR.split(" ");

        String folderName = null;
        String[][] pathsTreinoTeste = new String[MAX_EXECS][5];

        for (int i = 0; i < aprendizado.length; i++) {
            for (int l = 0; l < classificadores.length; l++) {
                
                loop_act:
                for (int j = 0; j < ordenacao.length; j++) {
                    for (int k = 0; k < selecao.length; k++) {
                        for (int execucao = 0; execucao < MAX_EXECS; execucao++) {

                            if (INPUT_MANUAL == false) {
                                //split  
                                split(props.getProperty("prop.inputNormal"),
                                        props.getProperty("prop.split.treino"), "splited");
                                //carrega z2    
                                z2 = io.openSplit(System.getProperty("user.dir").concat(File.separator).
                                        concat("splited").concat(File.separator).concat("treino.arff"));
                                //carrega z3
                                z3 = io.openSplit(System.getProperty("user.dir").concat(File.separator).
                                        concat("splited").concat(File.separator).concat("teste.arff"));
                            } else {
                                //carregar treino e teste das iterações do primeiro classificador
                                //INPUT_MANUAL = true;

                                List<Object> temp;
                                if (Boolean.valueOf(props.getProperty("prop.inputManual")) == true) {
                                    temp
                                            = inputManual(props.getProperty("prop.inputManual.treino"),//treino
                                                    props.getProperty("prop.inputManual.teste"),//teste
                                                    props.getProperty("prop.inputManual.fronteira"),//fronteira
                                                    props.getProperty("prop.inputManual.amostrasT"),//amostrasT
                                                    props.getProperty("prop.inputManual.vizinhosT")//vizinhosT
                                            );
                                } else {

                                    temp
                                            = inputManual(pathsTreinoTeste[execucao][0],//treino
                                                    pathsTreinoTeste[execucao][1],//teste
                                                    pathsTreinoTeste[execucao][2],//fronteira
                                                    pathsTreinoTeste[execucao][3],//amostrasT
                                                    pathsTreinoTeste[execucao][4]//vizinhosT
                                            );
                                }

                                z2 = (Instances) temp.get(0);
                                z3 = (Instances) temp.get(1);
                                fronteiras = (List<BeanAmostra>) temp.get(2);
                                amostrasT = (List<BeanAmostra>) temp.get(3);
                                vizinhosT = (List<BeanAmostra>) temp.get(4);

                            }

                            z2.setClassIndex(z2.numAttributes() - 1);
                            z3.setClassIndex(z3.numAttributes() - 1);

                            folderName = props.getProperty("prop.inputNormal").split("/")[props.getProperty("prop.inputNormal").split("/").length - 1].split(".arff")[0]
                                    + "_-_" + aprendizado[i] + "_-_" + ordenacao[j] + "_-_" + selecao[k] + "_-_" + classificadores[l] + "_-_exec_" + execucao;

                            pathsTreinoTeste[execucao][0]
                                    = System.getProperty("user.dir").concat(File.separator).
                                    concat(folderName).concat(File.separator).concat("treino.arff");

                            pathsTreinoTeste[execucao][1]
                                    = System.getProperty("user.dir").concat(File.separator).
                                    concat(folderName).concat(File.separator).concat("teste.arff");

                            pathsTreinoTeste[execucao][2]
                                    = System.getProperty("user.dir").concat(File.separator).
                                    concat(folderName).concat(File.separator).concat("fronteira.arff");

                            pathsTreinoTeste[execucao][3]
                                    = System.getProperty("user.dir").concat(File.separator).
                                    concat(folderName).concat(File.separator).concat("amostrasT.arff");

                            pathsTreinoTeste[execucao][4]
                                    = System.getProperty("user.dir").concat(File.separator).
                                    concat(folderName).concat(File.separator).concat("vizinhosT.arff");

                            //aprendizado
                            switch (aprendizado[i]) {
                                case "rand":
                                    new LearnRandom().random(z2, z3, XNUMCLASSES,
                                            classificadores[l]);
                                    break;
                                case "act":
                                    int kVizinhos = z2.numClasses() * XNUMCLASSES;
                                    new LearnActive().active(z2, z3, XNUMCLASSES, kVizinhos,
                                            ordenacao[j], classificadores[l], selecao[k], fronteiras,
                                            amostrasT, vizinhosT);
                                    break;
                            }
                            mov.mvExecucao(folderName, classificadores[l]);

                            System.err.println("Final execução " + (execucao + 1) + "/" + MAX_EXECS + "\n");
                        }
                        if(aprendizado[i].equals("rand")){
                            break loop_act;
                        }
                        INPUT_MANUAL = true;
                    }
                }
            }
        }
    }

    private static void split(String path, String pctTreinamento, String folder) {
        List<String> files = new ArrayList<>();
        Splitter splitter = new Splitter();
        splitter.main(new String[]{path, pctTreinamento});
        files.add("teste.arff");
        files.add("treino.arff");
        Movimentacao.mvSplit(files, folder);
    }

    private static List<Object> inputManual(String treino, String teste,
            String fronteira, String amostraT, String vizinhoT) {

        IOArff io = new IOArff();
        Instances z2 = null, z3 = null, fronteiraTemp = null, amostraTTemp = null, vizinhoTTemp = null;

        List<BeanAmostra> fronteiras = new ArrayList<>();
        List<BeanAmostra> amostrasT = new ArrayList<>();
        List<BeanAmostra> vizinhosT = new ArrayList<>();

        try {
            z2 = io.openSplit(treino);
            z3 = io.openSplit(teste);
            fronteiraTemp = io.openSplit(fronteira);
            amostraTTemp = io.openSplit(amostraT);
            vizinhoTTemp = io.openSplit(vizinhoT);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (int i = 0; i < fronteiraTemp.numInstances(); i++) {
            BeanAmostra amostraFronteira = new BeanAmostra();
            amostraFronteira.setAmostra(fronteiraTemp.get(i));
            fronteiras.add(amostraFronteira);
        }
        for (int i = 0; i < amostraTTemp.numInstances(); i++) {
            BeanAmostra amostraTT = new BeanAmostra();
            amostraTT.setAmostra(amostraTTemp.get(i));
            amostrasT.add(amostraTT);
        }
        for (int i = 0; i < vizinhoTTemp.numInstances(); i++) {
            BeanAmostra amostraVizinho = new BeanAmostra();
            amostraVizinho.setAmostra(vizinhoTTemp.get(i));
            vizinhosT.add(amostraVizinho);
        }

        List<String> files = new ArrayList<>();
        files.add(treino);
        files.add(teste);

        List<Object> ret = new ArrayList<>();
        ret.add(z2);
        ret.add(z3);
        ret.add(fronteiras);
        ret.add(amostrasT);
        ret.add(vizinhosT);

        Movimentacao.mvSplit(files, "splited");

        return ret;
    }

}
