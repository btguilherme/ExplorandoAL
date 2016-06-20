/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import io.IOArff;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
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

    private static final int XNUMCLASSES = 2;
    private static final int FOLDS = 8;
    //rand ou act
    private static final String OPC_APRENDIZADO = "act";
    private static final int MAX_EXECS = 1;
    private static final boolean INPUT_MANUAL = false;

    //opfsuper svmcross svmgrid opfsemi universvm
    private static final String classificador = "svmcross opfsuper";

    public static void main(String[] args) throws Exception {
        
        Movimentacao mov = new Movimentacao();
        IOArff io = new IOArff();

        Instances z2 = null, z3 = null;

        if (INPUT_MANUAL == true) {
            List<Instances> temp
                    = inputManual(
                            "",//TREINO
                            ""//TESTE
                    );
            z2 = temp.get(0);
            z3 = temp.get(1);
        }

        for (int execucao = 0; execucao < MAX_EXECS; execucao++) {

            if (INPUT_MANUAL == false) {
                //split
                //split("/home/guilherme/MineracaoDados/src/arffs/100leaves/data_Mar_64.arff",
                split("/home/guilherme/MineracaoDados/src/arffs/ecoli/ecoli_no_string_att_number_class.arff",
                        "80", "splited");
                //carrega z2    
                z2 = io.openSplit(System.getProperty("user.dir").concat(File.separator).
                        concat("splited").concat(File.separator).concat("treino.arff"));
                z2.setClassIndex(z2.numAttributes() - 1);
                //carrega z3
                z3 = io.openSplit(System.getProperty("user.dir").concat(File.separator).
                        concat("splited").concat(File.separator).concat("teste.arff"));
            }

            //aprendizado
            switch(OPC_APRENDIZADO){
                case "rand":
                    new LearnRandom().random(z2, z3, FOLDS, XNUMCLASSES, classificador);
                    break;
                case "act":
                    int kVizinhos = z2.numClasses() * XNUMCLASSES;
                    new LearnActive().active(z2, z3, FOLDS, XNUMCLASSES, classificador, kVizinhos);
                    break;
            }
            
            
            

            mov.mvExecucao(execucao, classificador);
            //mov.mv2TrashRaizes();
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
        Movimentacao mov = new Movimentacao();
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

        mov.mvSplit(files, "splited");

        return ret;
    }

}
