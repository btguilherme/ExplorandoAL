/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classificar;

import io.IOText;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instances;

/**
 *
 * @author guilherme
 */
public class ClassificadorSVM {
    
    public ClassificadorSVM(){}
    
    public Classifier makeTheSteps(Instances raizes, Instances z3){
        
        Classifier classifier = train(raizes);
        classify(classifier,raizes, z3);
        
        return classifier;
    }

    protected Classifier train(Instances raizes) {

        System.err.print("Treinando classificador ... ");
        long init = System.nanoTime();
        
        Classifier classificador = new SMO();
        try {
            classificador.buildClassifier(raizes);
        } catch (Exception ex) {
            Logger.getLogger(ClassificadorSVM.class.getName()).log(Level.SEVERE, null, ex);
        }
        long end = System.nanoTime();
        long diff = end - init;
        double time = (diff / 1000000000.0);
        new IOText().save(System.getProperty("user.dir").concat(File.separator),
                "tempoTreino", String.valueOf(time));
        System.err.println("feito");

        return classificador;
    }

    protected void classify(Classifier classificador, Instances raizes, Instances z3) {
        System.err.print("Testando classificador ... ");
        long init = System.nanoTime();
        
        Evaluation eval = null;
        try {
            eval = new Evaluation(raizes);
            eval.evaluateModel(classificador, z3);
        } catch (Exception ex) {
            Logger.getLogger(ClassificadorSVM.class.getName()).log(Level.SEVERE, null, ex);
        }
        double acc = eval.pctCorrect();
        long end = System.nanoTime();
        long diff = end - init;
        double time = (diff / 1000000000.0);
        new IOText().save(System.getProperty("user.dir").concat(File.separator),
                "tempoTeste", String.valueOf(time));
        new IOText().save(System.getProperty("user.dir").concat(File.separator),
                "acc", String.valueOf(acc));
        System.err.println("feito");
    }
 
}
