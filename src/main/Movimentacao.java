/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.File;
import java.util.List;
import utils.RunCommand;

/**
 *
 * @author guilherme
 */
public class Movimentacao {

    public void mvExecucao(int execucao, String classifiers) {

        if (classifiers.contains("opfsuper")) {
            movimenta(execucao, "opf_results");
        }
        if (classifiers.contains("svmcross")) {
            movimenta(execucao, "svm_results");
        }
        if (classifiers.contains("svmgrid")) {
            movimenta(execucao, "grid_results");
        }
        if (classifiers.contains("opfsemi")) {
            movimenta(execucao, "opfsemi_results");
        }
        if (classifiers.contains("semil")) {
            movimenta(execucao, "semil_results");
        }
        if (classifiers.contains("universvm")) {
            movimenta(execucao, "universvm_results");
        }
    }

    public static void mvSplit(List<String> files, String folder) {
        for (String file : files) {
            String command = "cp " + file + " " + folder;
            RunCommand.runCommand(command);
        }
    }

    public void mv2TrashRaizes() {
        String rm = "rm -- " + System.getProperty("user.dir").concat(File.separator) + "raizes*";
        RunCommand.runCommand(rm);
    }

    private void movimenta(int execucao, String folder) {

        String mkdir = "mkdir execution_" + execucao;
        if(!new File("execution_" + execucao).exists()){
            RunCommand.runCommand(mkdir);
        }
        
        

        String cp = "cp " + System.getProperty("user.dir") + "/splited/teste.arff "
                + System.getProperty("user.dir") + "/splited/treino.arff execution_" + execucao;
        RunCommand.runCommand(cp);

        String mv = "mv " + folder + "/ execution_" + execucao + "/";
        RunCommand.runCommand(mv);

        mkdir = "mkdir " + folder;
        RunCommand.runCommand(mkdir);
    }

}
