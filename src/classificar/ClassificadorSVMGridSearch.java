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
import weka.classifiers.meta.GridSearch;

import weka.core.Instances;
import weka.core.SelectedTag;

/**
 *
 * @author guilherme
 */
public class ClassificadorSVMGridSearch extends ClassificadorSVM {

    public ClassificadorSVMGridSearch() {
        super();
    }

    @Override
    public Classifier train(Instances raizes) {
        
        System.err.print("Treinando classificador ... ");
        long init = System.nanoTime();
        
        
        //ini - grid search
        GridSearch gs = new GridSearch();
        //Set the evaluation to Accuracy.
        gs.setEvaluation(new SelectedTag(GridSearch.EVALUATION_ACC, weka.classifiers.meta.GridSearch.TAGS_EVALUATION));
                //Set the filter to weka.filters.AllFilter since we don't need any special data processing and we
        //don't optimize the filter in this case (data gets always passed through filter!).
        
/////////////////////////////////////////gs.setFilter(new weka.filters.AllFilter());
        
        //Set weka.classifiers.functions.SMO as classifier with 
        //weka.classifiers.functions.supportVector.RBFKernel as kernel.
        weka.classifiers.functions.SMO classifier = new weka.classifiers.functions.SMO();
        classifier.setKernel(new weka.classifiers.functions.supportVector.RBFKernel());
        gs.setClassifier(classifier);
                //Set the XProperty to "classifier.c", XMin to "1", XMax to "16", XStep to "1" 
        //and the XExpression to "I". This will test the "C" parameter of SMO for the values from 1 to 16.
        gs.setXProperty("classifier.c");
        gs.setXMin(1.0);
        gs.setXMax(16.0);
        gs.setXStep(1.0);
        gs.setXExpression("I");
        //Set the YProperty to "classifier.kernel.gamma", YMin to "-5", YMax to "2", YStep to "1"
        //YBase to "10" and YExpression to "pow(BASE,I)". This will test the gamma of the RBFKernel with 
        //the values 10^-5, 10^-4,..,10^2.
        gs.setYProperty("classifier.kernel.gamma");
        gs.setYMin(-5.0);
        gs.setYMax(2.0);
        gs.setYStep(1.0);
        gs.setYBase(10.0);
        gs.setYExpression("pow(BASE,I)");
        Classifier classificador = null;
        try {
            gs.buildClassifier(raizes);
            classificador = gs.getBestClassifier();
            classificador.buildClassifier(raizes);
            weka.core.SerializationHelper.write("model.model", classificador);
        } catch (Exception ex) {
            Logger.getLogger(ClassificadorSVMGridSearch.class.getName()).log(Level.SEVERE, null, ex);
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
