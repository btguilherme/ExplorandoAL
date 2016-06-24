/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classificar;

import weka.classifiers.CollectiveEvaluation;
import weka.classifiers.collective.meta.Weighting;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/**
 *
 * @author guilherme
 */
public class TrainUnlabeledTest {

    public static void main(String[] args) throws Exception {
        
        // load training data, set class
        Instances train = DataSource.read("/home/guilherme/NetBeansProjects/Splitter/dist/labeled.arff");
        train.setClassIndex(train.numAttributes() - 1);

        // load unlabeled data, set class
        Instances unlabeled = DataSource.read("/home/guilherme/NetBeansProjects/Splitter/dist/unlabeled.arff");
        unlabeled.setClassIndex(unlabeled.numAttributes() - 1);

        // load test data, set class
        Instances test = DataSource.read("/home/guilherme/NetBeansProjects/Splitter/dist/teste.arff");
        test.setClassIndex(test.numAttributes() - 1);

        // configure classifier
        Weighting weighting = new Weighting();
        
        // build classifier
        weighting.buildClassifier(train, unlabeled);

        // evaluate classifier
        CollectiveEvaluation eval = new CollectiveEvaluation(train);
        eval.evaluateModel(weighting, test);
        System.out.println(eval.toSummaryString());
    }

}
