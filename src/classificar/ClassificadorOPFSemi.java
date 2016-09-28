/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classificar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import splitter.Splitter;
import utils.RunCommand;

/**
 *
 * @author guilherme
 */
public class ClassificadorOPFSemi extends ClassificadorOPF {

    public ClassificadorOPFSemi() {
        super();
    }

    @Override
    protected void train(String trainingSetPath) {

        split(trainingSetPath, "40"/*40%-labeled*/, "splitSemi");

        rename(System.getProperty("user.dir").concat(File.separator).
                concat("splitSemi").concat(File.separator));

        arff2ascii(System.getProperty("user.dir").concat(File.separator).concat("splitSemi")
                .concat(File.separator).concat("labeled.arff"), "_labeled");

        ascii2dat(System.getProperty("user.dir").concat(File.separator).concat("splitSemi")
                .concat(File.separator).concat("labeled.arff").concat("_labeled.opf"), System.getProperty("user.dir")
                .concat(File.separator).concat("Z1LINE"));

        arff2ascii(System.getProperty("user.dir").concat(File.separator).concat("splitSemi")
                .concat(File.separator).concat("unlabeled.arff"), "_unlabeled");
        ascii2dat(System.getProperty("user.dir").concat(File.separator).concat("splitSemi")
                .concat(File.separator).concat("unlabeled.arff").concat("_unlabeled.opf"), System.getProperty("user.dir")
                .concat(File.separator).concat("Z1DOUBLELINE"));


        String comando = "opf_semi " + System.getProperty("user.dir").concat(File.separator).concat("Z1LINE ")
                + System.getProperty("user.dir").concat(File.separator).concat("Z1DOUBLELINE");

        RunCommand.runCommand(comando);
    }
    
    protected void rename(String path) {
        String command = "cp " + path + "treino.arff " + path + "labeled.arff";
        RunCommand.runCommand(command);
        command = "cp " + path + "teste.arff " + path + "unlabeled.arff";
        RunCommand.runCommand(command);

        command = "rm " + path + "treino.arff";
        RunCommand.runCommand(command);
        command = "rm " + path + "teste.arff";
        RunCommand.runCommand(command);
    }

    protected void split(String path, String pctTreinamento, String folder) {
        List<String> files = new ArrayList<>();
        Splitter splitter = new Splitter();
        splitter.main(new String[]{path, pctTreinamento});
        files.add("teste.arff");
        files.add("treino.arff");
        mvSplit(files, folder);
    }

    protected void mvSplit(List<String> files, String folder) {
        for (String file : files) {
            String command = "mv " + file + " " + folder;
            RunCommand.runCommand(command);
        }
    }

}
