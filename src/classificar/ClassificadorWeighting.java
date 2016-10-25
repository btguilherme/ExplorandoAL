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
import weka.classifiers.collective.meta.Weighting;
import weka.core.Instances;

/**
 *
 * @author guilherme
 */
public class ClassificadorWeighting extends ClassificadorCollectiveWrapper {

    public ClassificadorWeighting() {
        super();
    }

    @Override
    public Classifier train(Instances raizes, Instances unlabeled) {
        System.err.print("Treinando classificador ... ");
        long init = System.nanoTime();

        Weighting weighting = new Weighting();

        try {
            // build classifier
            if (unlabeled == null) {
                weighting.buildClassifier(raizes);
                weka.core.SerializationHelper.write("model.model", weighting);
            } else {
                weighting.buildClassifier(raizes, unlabeled);
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

        return weighting;
    }

}
