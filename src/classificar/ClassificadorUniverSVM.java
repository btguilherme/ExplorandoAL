/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classificar;

import io.IOText;
import java.io.File;
import java.util.List;
import utils.RunCommand;

/**
 *
 * @author guilherme
 */
public class ClassificadorUniverSVM extends ClassificadorOPFSemi {

    public ClassificadorUniverSVM() {
        super();
    }

    @Override
    protected void train(String trainingSetPath) {

        split(trainingSetPath, "40"/*40%-labeled*/, "splitSemi");

        rename(System.getProperty("user.dir").concat(File.separator).
                concat("splitSemi").concat(File.separator));

        arff2ascii(System.getProperty("user.dir").concat(File.separator).concat("splitSemi")
                .concat(File.separator).concat("labeled.arff"), "_labeled");

        ascii2svm(System.getProperty("user.dir").concat(File.separator).concat("splitSemi")
                .concat(File.separator).concat("labeled.arff_labeled.opf"), System.getProperty("user.dir")
                .concat(File.separator).concat("labeled.svm"));

        arff2ascii(System.getProperty("user.dir").concat(File.separator).concat("splitSemi")
                .concat(File.separator).concat("unlabeled.arff"), "_unlabeled");

        ascii2svm(System.getProperty("user.dir").concat(File.separator).concat("splitSemi")
                .concat(File.separator).concat("unlabeled.arff_unlabeled.opf"), System.getProperty("user.dir")
                .concat(File.separator).concat("unlabeled.svm"));

//        String comando = "universvm -u " + System.getProperty("user.dir")
//                .concat(File.separator).concat("unlabeled.svm.txt") + " " + System.getProperty("user.dir")
//                .concat(File.separator).concat("labeled.svm.txt") + " model";
//
//        long init = System.nanoTime();
//        RunCommand.runCommand(comando);
//        long end = System.nanoTime();
//        long diff = end - init;
//        double time = (diff / 1000000000.0);//tempo de selecao
//        new IOText().save(System.getProperty("user.dir").concat(File.separator),
//                "training", String.valueOf(time));
    }

    @Override
    protected void classify(String testSetPath) {
        arff2ascii(testSetPath, "_test");

        ascii2svm(testSetPath + "_test.opf", testSetPath + "_test.svm");

        //String comando = "universvm -F model "+testSetPath + "_test.svm.txt";
        String comando = "universvm -v 10 -f outputUniverSVM.txt -u " + System.getProperty("user.dir")
                .concat(File.separator).concat("unlabeled.svm.txt") + " -T " 
                + testSetPath + "_test.svm.txt " + System.getProperty("user.dir")
                .concat(File.separator).concat("labeled.svm.txt");

//        String comando = "universvm -T " + testSetPath + "_test.svm.txt"
//                + " -f outputUniverSVM.txt " + System.getProperty("user.dir")
//                .concat(File.separator).concat("labeled.svm.txt") + " -F model";
        long init = System.nanoTime();
        RunCommand.runCommand(comando);
        long end = System.nanoTime();
        long diff = end - init;
        double time = (diff / 1000000000.0);//tempo de selecao
        new IOText().save(System.getProperty("user.dir").concat(File.separator),
                "testing", String.valueOf(time));
    }

    @Override
    protected void accuracy() {
        IOText io = new IOText();
        List<String> file = io.open(System.getProperty("user.dir").
                concat(File.separator).concat("outputUniverSVM.txt"));

        String acc = null;
        for (String line : file) {
            if (line.contains("mean accuracy")) {
                acc = line.split("=")[1];
                break;
            }
        }
        new IOText().save(System.getProperty("user.dir").concat(File.separator),
                "acuracy", acc);
    }

    private void ascii2svm(String ascii, String svm) {
        String command = "opf2svm " + ascii + " " + svm;
        RunCommand.runCommand(command);
    }
}
