/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package learning;

import java.util.List;
import weka.core.Instances;

/**
 *
 * @author guilherme
 */
public interface ILearn {

    int numClassesConhecidas(Instances raizes);//random

    int numClassesConhecidas(List<Double> dicionario, Instances raizes);//active
}
