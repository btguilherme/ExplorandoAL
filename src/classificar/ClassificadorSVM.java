/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classificar;

import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Debug;
import weka.core.Instances;

/**
 *
 * @author guilherme
 */
public class ClassificadorSVM {
    
    protected String results;
    
    public ClassificadorSVM(){}
    
    public String makeTheSteps(Instances raizes, Instances z3, int folds){
        this.results = "";
        z3.setClassIndex(z3.numAttributes() - 1);
        Classifier classifier = train(raizes);
        Evaluation eval = classify(classifier,raizes, z3, folds);
        this.results = this.results.concat(String.valueOf(accuracy(eval))).concat("\t");
        
        return this.results;
    }

    protected Classifier train(Instances raizes) {

        Classifier classificador = new SMO();
        long init = System.nanoTime();
        try {
            classificador.buildClassifier(raizes);
        } catch (Exception ex) {
            Logger.getLogger(ClassificadorSVM.class.getName()).log(Level.SEVERE, null, ex);
        }

        long end = System.nanoTime();
        long diff = end - init;
        double time = (diff / 1000000000.0);

        this.results = this.results.concat(String.valueOf(time)).concat("\t");
        
        return classificador;
    }

    protected Evaluation classify(Classifier classificador, Instances raizes, Instances z3, int folds) {
        Evaluation eval = null;
        long init = System.nanoTime();
        try {
            eval = new Evaluation(raizes);
            eval.evaluateModel(classificador, z3);
            eval.crossValidateModel(classificador, raizes, folds, new Debug.Random());
        } catch (Exception ex) {
            Logger.getLogger(ClassificadorSVM.class.getName()).log(Level.SEVERE, null, ex);
        }
        long end = System.nanoTime();
        long diff = end - init;
        double time = (diff / 1000000000.0);

        this.results = this.results.concat(String.valueOf(time)).concat("\t");
        
        return eval;
    }

    protected double accuracy(Evaluation eval) {
        return eval.pctCorrect();
    }

}
