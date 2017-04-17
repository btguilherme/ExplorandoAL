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
        String AGRUPAMENTO = props.getProperty("prop.agrupamento");
     
        Movimentacao mov = new Movimentacao();
        IOArff io = new IOArff();

        Instances z2 = null, z2i = null, z2ii = null, z3 = null;
        List<BeanAmostra> fronteiras = null, amostrasT = null, vizinhosT = null;

//        if (INPUT_MANUAL == true) {
//            List<Object> temp
//                    = inputManual(
//                            props.getProperty("prop.inputManual.treino"),
//                            props.getProperty("prop.inputManual.teste"),
//                            props.getProperty("prop.inputManual.fronteira"),
//                            props.getProperty("prop.inputManual.amostrasT"),
//                            props.getProperty("prop.inputManual.vizinhosT")
//                    );
//            z2 = (Instances) temp.get(0);
//            z3 = (Instances) temp.get(1);
//            fronteiras = (List<BeanAmostra>) temp.get(2);
//            amostrasT = (List<BeanAmostra>) temp.get(3);
//            vizinhosT = (List<BeanAmostra>) temp.get(4);
//        }

        String[] aprendizado = OPC_APRENDIZADO.split(" ");
        String[] ordenacao = ORDENACAO.split(" ");
        String[] selecao = METODOSELECAO.split(" ");
        String[] classificadores = CLASSIFICADOR.split(" ");
        String[] agrupamentos = AGRUPAMENTO.split(" ");

        String folderName = null;
        String[][] pathsTreinoTeste = new String[MAX_EXECS][7];

        for (int i = 0; i < aprendizado.length; i++) {
            for (int l = 0; l < classificadores.length; l++) {

                loop_act:
                for (int j = 0; j < ordenacao.length; j++) {
                    for (int k = 0; k < selecao.length; k++) {
                        for (int m = 0; m < agrupamentos.length; m++) {

                            for (int execucao = 0; execucao < MAX_EXECS; execucao++) {
                                
                                System.err.print(aprendizado[i]+",");
                                System.err.print(classificadores[l]+",");
                                System.err.print(ordenacao[j]+",");
                                System.err.print(selecao[k]+",");
                                System.err.println(agrupamentos[m]);
                                
                                if (INPUT_MANUAL == false) {
                                    //split  
                                    split(props.getProperty("prop.inputNormal"),
                                            props.getProperty("prop.split.treino"), "splited", "treino", "teste");
                                    //carrega z2    
                                    z2 = io.openSplit(System.getProperty("user.dir").concat(File.separator).
                                            concat("splited").concat(File.separator).concat("treino.arff"));
                                    //carrega z3
                                    z3 = io.openSplit(System.getProperty("user.dir").concat(File.separator).
                                            concat("splited").concat(File.separator).concat("teste.arff"));
                                    
                                    
                                    split(System.getProperty("user.dir").
                                            concat(File.separator).concat("splited").concat(File.separator).
                                            concat("treino.arff"),"50", "splited", "z2i", "z2ii");
                                    
                                    z2i = io.openSplit(System.getProperty("user.dir").concat(File.separator).
                                                concat("splited").concat(File.separator).concat("z2i.arff"));
                                    
        
                                    z2ii = io.openSplit(System.getProperty("user.dir").concat(File.separator).
                                                concat("splited").concat(File.separator).concat("z2ii.arff")); 
                                    
                                    
                                } else {
                                //carregar treino e teste das iterações do primeiro classificador
                                    //INPUT_MANUAL = true;

                                    List<Object> temp;
                                    if (Boolean.valueOf(props.getProperty("prop.inputManual")) == true) {
                                        temp
                                                = inputManual(
                                                        props.getProperty("prop.inputManual.path").concat(execucao+"/treino.arff"),//treino
                                                        props.getProperty("prop.inputManual.path").concat(execucao+"/teste.arff"),//teste
                                                        props.getProperty("prop.inputManual.path").concat(execucao+"/fronteira.arff"),//fronteira
                                                        props.getProperty("prop.inputManual.path").concat(execucao+"/amostrasT.arff"),//amostrasT
                                                        props.getProperty("prop.inputManual.path").concat(execucao+"/vizinhosT.arff"),//vizinhosT
                                                        props.getProperty("prop.inputManual.path").concat(execucao+"/z2i.arff"),//vizinhosT
                                                        props.getProperty("prop.inputManual.path").concat(execucao+"/z2ii.arff")//vizinhosT
                                                );
                                    } else {

                                        temp
                                                = inputManual(pathsTreinoTeste[execucao][0],//treino
                                                        pathsTreinoTeste[execucao][1],//teste
                                                        pathsTreinoTeste[execucao][2],//fronteira
                                                        pathsTreinoTeste[execucao][3],//amostrasT
                                                        pathsTreinoTeste[execucao][4],//vizinhosT
                                                        pathsTreinoTeste[execucao][5],//z2i
                                                        pathsTreinoTeste[execucao][6]//z2ii
                                                );
                                    }

                                    z2 = (Instances) temp.get(0);
                                    z3 = (Instances) temp.get(1);
                                    fronteiras = (List<BeanAmostra>) temp.get(2);
                                    amostrasT = (List<BeanAmostra>) temp.get(3);
                                    vizinhosT = (List<BeanAmostra>) temp.get(4);
                                    z2i = (Instances) temp.get(5);
                                    z2ii = (Instances) temp.get(6);

                                }

                                z2i.setClassIndex(z2i.numAttributes() - 1);
                                z2.setClassIndex(z2.numAttributes() - 1);
                                z3.setClassIndex(z3.numAttributes() - 1);
                                
                
                                folderName = props.getProperty("prop.inputNormal").split("/")[props.getProperty("prop.inputNormal").split("/").length - 1].split(".arff")[0]
                                        + "_-_" + aprendizado[i] + "_-_"+ agrupamentos[m]+"_-_" + ordenacao[j] + "_-_" + selecao[k] + "_-_" + classificadores[l] + "_-_exec_" + execucao;

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
                                
                                pathsTreinoTeste[execucao][5]
                                        = System.getProperty("user.dir").concat(File.separator).
                                        concat(folderName).concat(File.separator).concat("z2i.arff");
                                
                                pathsTreinoTeste[execucao][6]
                                        = System.getProperty("user.dir").concat(File.separator).
                                        concat(folderName).concat(File.separator).concat("z2ii.arff");

                                //aprendizado
                                switch (aprendizado[i]) {
                                    case "rand":
                                        new LearnRandom().random(z2, z3, XNUMCLASSES,
                                                classificadores[l]);
                                        break;
                                    case "act":
                                        int kVizinhos = z2.numClasses()/2;// * XNUMCLASSES;
                                        new LearnActive().active(z2, z2i, z2ii, z3, XNUMCLASSES, kVizinhos,
                                                ordenacao[j], classificadores[l], selecao[k], fronteiras,
                                                amostrasT, vizinhosT, agrupamentos[m]);
                                        break;
                                }
                                mov.mvExecucao(folderName, classificadores[l]);

                                System.err.println("Final execução " + (execucao + 1) + "/" + MAX_EXECS + "\n");
                            }
                            if (aprendizado[i].equals("rand")) {
                                break loop_act;
                            }
                            INPUT_MANUAL = true;
                        }
                    }
                }
            }
        }
    }

    private static void split(String path, String pctTreinamento, String folder,
            String name1, String name2) {
        
        
        
        List<String> files = new ArrayList<>();
        Splitter splitter = new Splitter();
        splitter.main(new String[]{path, pctTreinamento, name1, name2});
        files.add(name1 + ".arff");
        files.add(name2 + ".arff");
        Movimentacao.mvSplit(files, folder);
    }

    private static List<Object> inputManual(String treino, String teste,
            String fronteira, String amostraT, String vizinhoT, String z2i, 
            String z2ii) {

        IOArff io = new IOArff();
        Instances z2 = null, z3 = null, fronteiraTemp = null, amostraTTemp = null, 
                vizinhoTTemp = null, z2iTemp = null, z2iiTemp = null;

        List<BeanAmostra> fronteiras = new ArrayList<>();
        List<BeanAmostra> amostrasT = new ArrayList<>();
        List<BeanAmostra> vizinhosT = new ArrayList<>();

        try {
            z2 = io.openSplit(treino);
            z3 = io.openSplit(teste);
            fronteiraTemp = io.openSplit(fronteira);
            amostraTTemp = io.openSplit(amostraT);
            vizinhoTTemp = io.openSplit(vizinhoT);
            z2iTemp = io.openSplit(z2i);
            z2iiTemp = io.openSplit(z2ii);
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
        files.add(z2i);
        files.add(z2ii);

        List<Object> ret = new ArrayList<>();
        ret.add(z2);
        ret.add(z3);
        ret.add(fronteiras);
        ret.add(amostrasT);
        ret.add(vizinhosT);
        ret.add(z2iTemp);
        ret.add(z2iiTemp);

        Movimentacao.mvSplit(files, "splited");

        return ret;
    }

}
