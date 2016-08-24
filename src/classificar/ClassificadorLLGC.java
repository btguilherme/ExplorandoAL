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
import weka.classifiers.collective.functions.LLGC;
import weka.core.Instances;

/**
 *
 * @author guilherme
 */
public class ClassificadorLLGC implements IClassificadorSemiSuper {

    @Override
    public Classifier makeTheSteps(Instances raizes, Instances z3, Instances unlabeled) {
        Instances mergedTest = merge(z3, unlabeled);
        Classifier classifier = train(raizes, mergedTest);
        classify(classifier, raizes, z3);

        return classifier;
    }

    @Override
    public Classifier train(Instances raizes, Instances unlabeled) {

        System.err.print("Treinando classificador ... ");
        long init = System.nanoTime();

        LLGC llgc = new LLGC();
        try {
            // build classifier
            llgc.buildClassifier(raizes, unlabeled);
        } catch (Exception ex) {
            Logger.getLogger(ClassificadorLLGC.class.getName()).log(Level.SEVERE, null, ex);
        }

        long end = System.nanoTime();
        long diff = end - init;
        double time = (diff / 1000000000.0);
        new IOText().save(System.getProperty("user.dir").concat(File.separator),
                "tempoTreino", String.valueOf(time));
        System.err.println("feito");

        return llgc;
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
            Logger.getLogger(ClassificadorLLGC.class.getName()).log(Level.SEVERE, null, ex);
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

    protected static Instances merge(Instances test, Instances unlabeled) {
        if (unlabeled != null) {
            for (int i = 0; i < unlabeled.numInstances(); i++) {
                test.add(unlabeled.instance(i));
            }
        }
        return test;
    }

}
