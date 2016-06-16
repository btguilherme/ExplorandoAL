/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package raizes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author guilherme
 */
public class Raizes {

    public List<Instances> shuffle(Instances z2, int xNumClasses){
        int numInstancias = z2.attribute(z2.numAttributes() - 1).numValues() * xNumClasses;
        Instances z1 = new Instances(z2, numInstancias);
        
        Instances aux = new Instances(z2, z2.numInstances());
        List<Instance> instances = new ArrayList<>();

        for (int i = 0; i < z2.numInstances(); i++) {
            instances.add(z2.instance(i));
        }

        Collections.shuffle(instances);

        for (Instance instance : instances) {
            aux.add(instance);
        }
        
        for (int i = 0; i < numInstancias; i++) {
            z1.add(aux.instance(i));
        }
        for (int i = 0; i < numInstancias; i++) {
            aux.delete(i);
        }
        z1.setClassIndex(z1.numAttributes() - 1);
        
        //aux é o novo z2
        List<Instances> conjuntos = new ArrayList<>();
        conjuntos.add(z1);
        conjuntos.add(aux);
        
        return conjuntos;
    }

}
