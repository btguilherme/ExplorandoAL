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
	FileInputStream file = 
                new FileInputStream(System.getProperty("user.dir").concat(File.separator)
                        + "/src/properties/propriedades.properties");
	props.load(file);
        
        int XNUMCLASSES = Integer.valueOf(props.getProperty("prop.xnumclasses"));
        int FOLDS = Integer.valueOf(props.getProperty("prop.folds"));
        String OPC_APRENDIZADO = props.getProperty("prop.aprendizado");
        int MAX_EXECS = Integer.valueOf(props.getProperty("prop.execucoes"));
        boolean INPUT_MANUAL = Boolean.valueOf(props.getProperty("prop.inputManual"));
        String ORDENACAO = props.getProperty("prop.ordenacao");
        String CLASSIFICADOR = props.getProperty("prop.classificador");
        
        Movimentacao mov = new Movimentacao();
        IOArff io = new IOArff();

        Instances z2 = null, z3 = null;

        if (INPUT_MANUAL == true) {
            List<Instances> temp
                    = inputManual(
                            props.getProperty("prop.inputManual.treino"),
                            props.getProperty("prop.inputManual.teste")
                    );
            z2 = temp.get(0);
            z3 = temp.get(1);
        }

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
            }
            
            z2.setClassIndex(z2.numAttributes() - 1);

            //aprendizado
            switch(OPC_APRENDIZADO){
                case "rand":
                    new LearnRandom().random(z2, z3, FOLDS, /*XNUMCLASSES*/1, CLASSIFICADOR);
                    break;
                case "act":
                    int kVizinhos = z2.numClasses() * XNUMCLASSES;
                    new LearnActive().active(z2, z3, FOLDS, XNUMCLASSES, CLASSIFICADOR, kVizinhos, ORDENACAO);
                    break;
            }

            Thread.sleep(400);
            mov.mvExecucao(execucao, CLASSIFICADOR);
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

    private static List<Instances> inputManual(String treino, String teste) {

        IOArff io = new IOArff();
        Instances z2 = null, z3 = null;

        try {
            z2 = io.openSplit(treino);
            z3 = io.openSplit(teste);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        List<String> files = new ArrayList<>();
        files.add(treino);
        files.add(teste);

        List<Instances> ret = new ArrayList<>();
        ret.add(z2);
        ret.add(z3);

        Movimentacao.mvSplit(files, "splited");

        return ret;
    }

}
