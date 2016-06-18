/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classificar;

import arff2opf.Arff2opf;
import java.io.File;
import utils.RunCommand;

/**
 *
 * @author guilherme
 */
public class ClassificadorOPF {
    
    public ClassificadorOPF(){}
    
    public void makeTheSteps(String raizes) {
        
        train(System.getProperty("user.dir").concat(File.separator).
                concat(raizes + ".arff"));
        
        classify(System.getProperty("user.dir").concat(File.separator).
                concat("splited").concat(File.separator).concat("teste.arff"));
        
        accuracy();
    }
    
    protected void train(String trainingSetPath) {
        arff2ascii(trainingSetPath, "_train");
        ascii2dat(trainingSetPath + "_train.opf", System.getProperty("user.dir")
                .concat(File.separator).concat("training"));
        String comando = "opf_train " + System.getProperty("user.dir")
                .concat(File.separator).concat("training");
        RunCommand.runCommand(comando);
    }
    
    protected void classify(String testSetPath) {
        arff2ascii(testSetPath, "_test");
        ascii2dat(testSetPath + "_test.opf", System.getProperty("user.dir")
                .concat(File.separator).concat("testing"));
        String comando = "opf_classify " + System.getProperty("user.dir")
                .concat(File.separator).concat("testing");
        RunCommand.runCommand(comando);
    }
    
    protected void accuracy() {
        String comando = "opf_accuracy " + System.getProperty("user.dir")
                .concat(File.separator).concat("testing");
        RunCommand.runCommand(comando);
    }
    
    protected void arff2ascii(String path, String opt) {
        //arrf2opf ascii
        String[] args = {path, path + opt};
        
        Arff2opf a2o1 = new Arff2opf();
        a2o1.main(args);
        while (a2o1.isFinished() == false) {/**/
            
        }
    }
    
    protected void ascii2dat(String path, String dat) {
        String comando = "txt2opf " + path + " " + dat;
        RunCommand.runCommand(comando);
    }
    
}
