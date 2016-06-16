/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classificar;

import java.io.File;

/**
 *
 * @author guilherme
 */
public class ClassificadorUniverSVM extends ClassificadorOPFSemi {

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
    }
    
    @Override
    protected void classify(String testSetPath) {
        arff2ascii(testSetPath, "_test");
        
        ascii2svm(testSetPath + "_test.opf", testSetPath + "_test.svm");
        
        String comando = "universvm -u unlabeled.svm.txt -f outputUniverSVM.txt -T "+testSetPath+"_test.svm.txt labeled.svm.txt";
        
        runCommand(comando);
    }
    
    @Override
    protected void accuracy() {
        
    }

    private void ascii2svm(String ascii, String svm) {
        String command = "opf2svm " + ascii + " " + svm;
        runCommand(command);
    }
}
