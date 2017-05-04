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

    public void mvExecucao(String folderDst, String classifiers) {

        switch (classifiers) {
            
            case "svmcross":
                movimenta(folderDst, "svm_results");
                break;
            case "svmgrid":
                movimenta(folderDst, "grid_results");
                break;
            case "CollectiveWrapper":
                movimenta(folderDst, "CollectiveWrapper_results");
                break;
            case "Weighting":
                movimenta(folderDst, "Weighting_results");
                break;
            case "YATSI":
                movimenta(folderDst, "YATSI_results");
                break;
            case "RF":
                movimenta(folderDst, "RF_results");
                break;
        }
    }

    public static void mvSplit(List<String> files, String folder) {
        for (String file : files) {
            //String command = "cp " + file + " " + folder;
            //RunCommand.runCommand(command);
            RunCommand.runCommand("cp", file, folder);
        }
    }

    private void movimenta(String folderDst, String folder) {

        String mkdir = "mkdir " + folderDst;
        if (!new File(folderDst).exists()) {
            RunCommand.runCommand(mkdir);
        }

        exec("cp",
                System.getProperty("user.dir") + "/splited/treino.arff",
                System.getProperty("user.dir") + "/" + folderDst);

        exec("cp",
                System.getProperty("user.dir") + "/splited/teste.arff",
                System.getProperty("user.dir") + "/" + folderDst);
        
        exec("cp",
                System.getProperty("user.dir") + "/splited/z2i.arff",
                System.getProperty("user.dir") + "/" + folderDst);
        
        exec("cp",
                System.getProperty("user.dir") + "/splited/z2ii.arff",
                System.getProperty("user.dir") + "/" + folderDst);
        
        exec("cp",
                System.getProperty("user.dir") + "/fronteira.arff",
                System.getProperty("user.dir") + "/" + folderDst);
        
        exec("cp",
                System.getProperty("user.dir") + "/vizinhosT.arff",
                System.getProperty("user.dir") + "/" + folderDst);
        
        exec("cp",
                System.getProperty("user.dir") + "/amostrasT.arff",
                System.getProperty("user.dir") + "/" + folderDst);
       
        exec("mv",
                folder + "/*",
                folderDst + "/");

    }

    /**
     * Executa um comando no terminal. Utilizar para comandos que utilizem dois
     * parâmetros de entrada (src e dst)
     *
     * @param comando Comando que será utilizado, por exemplo: mv, cp, etc
     * @param src Path do aquivo (txt, arff, etc)
     * @param dst Path do diretório
     */
    public static void exec(String comando, String src, String dst) {
        //RunCommand.runCommand(comando+" "+src+" "+dst);
        RunCommand.runCommand(comando, src, dst);
    }
}
