/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classificar;

import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 *
 * @author guilherme
 */
public interface IClassificadorSemiSuper {
    public Classifier makeTheSteps(Instances raizes, Instances z3, Instances unlabeled);
    public Classifier train(Instances raizes, Instances unlabeled);
    public void classify(Classifier classificador, Instances raizes, Instances z3);
}
