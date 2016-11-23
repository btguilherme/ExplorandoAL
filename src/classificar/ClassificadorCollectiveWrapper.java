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
import weka.classifiers.CollectiveEvaluation;
import weka.classifiers.collective.meta.CollectiveWrapper;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

/**
 *
 * @author guilherme
 */
public class ClassificadorCollectiveWrapper implements IClassificadorSemiSuper {

    @Override
    public Classifier makeTheSteps(Instances raizes, Instances z3, Instances unlabeled) {
        Classifier classifier = train(raizes, unlabeled);
        classify(classifier, raizes, z3);
        
        return classifier;
    }

    @Override
    public Classifier train(Instances raizes, Instances unlabeled) {
        System.err.print("Treinando classificador ... ");
        long init = System.nanoTime();

        CollectiveWrapper wrapper = new CollectiveWrapper();
        wrapper.setClassifier(new SMO());
        
        try {
            // build classifier
            if(unlabeled == null){
                wrapper.buildClassifier(raizes);
                weka.core.SerializationHelper.write("model.model", wrapper);
            }else{
                wrapper.buildClassifier(raizes, unlabeled);
            }
        } catch (Exception ex) {
            Logger.getLogger(ClassificadorCollectiveWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        long end = System.nanoTime();
        long diff = end - init;
        double time = (diff / 1000000000.0);
        new IOText().save(System.getProperty("user.dir").concat(File.separator),
                "tempoTreino", String.valueOf(time));
        System.err.println("feito");
        
        return wrapper;
    }

    @Override
    public void classify(Classifier classificador, Instances raizes, Instances z3) {
        
        System.err.print("Testando classificador ... ");
        long init = System.nanoTime();

        CollectiveEvaluation eval = null;
        try {
            eval = new CollectiveEvaluation(raizes);
            eval.evaluateModel(classificador, z3);
        } catch (Exception ex) {
            Logger.getLogger(ClassificadorCollectiveWrapper.class.getName()).log(Level.SEVERE, null, ex);
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
