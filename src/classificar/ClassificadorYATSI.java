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
import weka.classifiers.collective.meta.YATSI;
import weka.core.Instances;

/**
 *
 * @author guilherme
 */
public class ClassificadorYATSI extends ClassificadorLLGC {

    public ClassificadorYATSI() {
        super();
    }

    @Override
    public Classifier train(Instances raizes, Instances unlabeled) {

        System.err.print("Treinando classificador ... ");
        long init = System.nanoTime();

        YATSI yatsi = new YATSI();
        yatsi.setKNN(10);
        yatsi.setNoWeights(true);
        try {
            // build classifier
            if (unlabeled == null) {
                yatsi.buildClassifier(raizes);
            } else {
                yatsi.buildClassifier(raizes, unlabeled);
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

        return yatsi.getClassifier();
    }

}
