/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import classificar.ClassificadorOPF;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author guilherme
 */
public class Movimentacao {

    public void mvExecucao(int execucao, String classifiers) {

        if(classifiers.contains("opfsuper")){
            movimenta(execucao, "opf_results");
        }
        if(classifiers.contains("svmcross")){
            movimenta(execucao, "svm_results");
        }
        if(classifiers.contains("svmgrid")){
            movimenta(execucao, "grid_results");
        }
        if(classifiers.contains("opfsemi")){
            movimenta(execucao, "opfsemi_results");
        }
        if(classifiers.contains("semil")){
            movimenta(execucao, "semil_results");
        }
        if(classifiers.contains("universvm")){
            movimenta(execucao, "universvm_results");
        }
    }

    public static void mvSplit(List<String> files, String folder) {
        for (String file : files) {
            String command = "cp " + file + " " + folder;
            runCommand(command);
        }
    }

    public void mv2TrashRaizes(){
        String rm = "rm ./raizes* "+System.getProperty("user.dir");
        runCommand(rm);
    }
    
    private void movimenta(int execucao, String folder) {

        String mkdir = "mkdir execution_" + execucao;
        runCommand(mkdir);
        
        String cp = "cp " + System.getProperty("user.dir") + "/splited/teste.arff "
                + System.getProperty("user.dir") + "/splited/treino.arff execution_" + execucao;
        runCommand(cp);
        
        String mv = "mv " + folder + "/ execution_" + execucao + "/";
        runCommand(mv);
        
        mkdir = "mkdir " + folder;
        runCommand(mkdir);
    }

    private static void runCommand(String comando) {
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

}
