/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classificar;

import weka.classifiers.CollectiveEvaluation;
import weka.classifiers.collective.functions.LLGC;
import weka.classifiers.collective.lazy.CollectiveIBk;
import weka.classifiers.collective.meta.Chopper;
import weka.classifiers.collective.meta.CollectiveEM;
import weka.classifiers.collective.meta.CollectiveWrapper;
import weka.classifiers.collective.meta.FilteredCollectiveClassifier;
import weka.classifiers.collective.meta.Weighting;
import weka.classifiers.collective.meta.YATSI;
import weka.classifiers.collective.trees.CollectiveForest;
import weka.classifiers.collective.trees.CollectiveTree;
import weka.core.EuclideanDistance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/**
 *
 * @author guilherme
 */
public class TrainUnlabeledTest {

    public static void main(String[] args) throws Exception {
        
        // load training data, set class
        Instances train = DataSource.read("/media/guilherme/Arquivos/backupElementaryOS/NetBeansProjects/ExplorandoAL/execution_0/Weighting_results/it0/raizes0.arff");
        train.setClassIndex(train.numAttributes() - 1);

        // load unlabeled data, set class
        Instances unlabeled = DataSource.read("/media/guilherme/Arquivos/backupElementaryOS/NetBeansProjects/ExplorandoAL/execution_0/Weighting_results/it0/unlabeled.arff");
        unlabeled.setClassIndex(unlabeled.numAttributes() - 1);

        // load test data, set class
        Instances test = DataSource.read("/media/guilherme/Arquivos/backupElementaryOS/NetBeansProjects/ExplorandoAL/execution_0/teste.arff");
        test.setClassIndex(test.numAttributes() - 1);

        // configure classifier
        
        //ok
//        LLGC llgc = new LLGC();
//        Instances mergedTest = merge(test, unlabeled);
//        // build classifier
//        llgc.buildClassifier(train, mergedTest);
//        // evaluate classifier
//        CollectiveEvaluation eval = new CollectiveEvaluation(train);
//        eval.evaluateModel(llgc, test);
//        System.out.println(eval.toSummaryString());
        
        
        //ok
//        CollectiveWrapper wrapper = new CollectiveWrapper();
//        // build classifier
//        wrapper.buildClassifier(train, unlabeled);
//        // evaluate classifier
//        CollectiveEvaluation eval = new CollectiveEvaluation(train);
//        eval.evaluateModel(wrapper, test);
//        System.out.println(eval.toSummaryString());
        
        //ok
        Weighting weighting = new Weighting();
        // build classifier
        weighting.buildClassifier(train, unlabeled);
        // evaluate classifier
        CollectiveEvaluation eval = new CollectiveEvaluation(train);
        eval.evaluateModel(weighting, test);
        System.out.println(eval.toSummaryString());

        //ok
//        YATSI yatsi = new YATSI();
//        yatsi.setKNN(10);
//        yatsi.setNoWeights(true);
//        
//        // build classifier
//        yatsi.buildClassifier(train, unlabeled);
//        // evaluate classifier
//        CollectiveEvaluation eval = new CollectiveEvaluation(train);
//        eval.evaluateModel(yatsi, test);
//        System.out.println(eval.toSummaryString());
      
        
        
        /*
            "That was a limitation of some of the algorithms, that an identical (unlabeled) instance has to be present."
            It means that every instance in unlabeled set has to be in test set?
                https://github.com/fracpete/collective-classification-weka-package/issues/1
        */
//        FilteredCollectiveClassifier fcc = new FilteredCollectiveClassifier();
//        Instances mergedTest = merge(test, unlabeled);
//        // build classifier
//        fcc.buildClassifier(train, mergedTest);
//        // evaluate classifier
//        CollectiveEvaluation eval = new CollectiveEvaluation(train);
//        eval.evaluateModel(fcc.getClassifier(), test);
//        System.out.println(eval.toSummaryString());
        
    }

    private static Instances merge(Instances test, Instances unlabeled) {
        for (int i = 0; i < unlabeled.numInstances(); i++) {
            test.add(unlabeled.instance(i));
        }
        return test;
    }

}
