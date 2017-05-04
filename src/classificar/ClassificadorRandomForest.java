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
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

/**
 *
 * @author guilherme
 */
public class ClassificadorRandomForest extends ClassificadorSVM {
    
    public ClassificadorRandomForest(){
    }
    
    @Override
    protected Classifier train(Instances raizes) {

        System.err.print("Treinando classificador ... ");
        long init = System.nanoTime();

        Classifier classificador = new RandomForest();
        
        
        try {
            classificador.buildClassifier(raizes);
            //weka.core.SerializationHelper.write("model.model", classificador);
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
    
}
