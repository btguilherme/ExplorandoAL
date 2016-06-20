/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package learning;

import io.IOArff;
import java.util.List;
import raizes.Raizes;
import weka.core.Instances;

/**
 *
 * @author guilherme
 */
public class LearnRandom extends Learn {

    public void random(Instances z2, Instances z3, int folds,
            int xNumClasses, String classifiers) {

        IOArff ioArff = new IOArff();

        int numInstancias = z2.attribute(z2.numAttributes() - 1).numValues() * xNumClasses;
        int iteration = 0;

        Raizes r = new Raizes();
        List<Instances> conjuntos = r.shuffle(z2, xNumClasses);
        Instances raizes = conjuntos.get(0);

        //atualiza z2 (amostras de z2 original menos amostras raizes de z1)
        z2 = conjuntos.get(1);

        do {
            long init = System.nanoTime();
            if (iteration != 0) {
                for (int i = 0; i < numInstancias; i++) {
                    raizes.add(z2.instance(i));
                }
                for (int i = 0; i < numInstancias; i++) {
                    z2.delete(i);
                }
            }
            long end = System.nanoTime();
            long diff = end - init;
            double time = (diff / 1000000000.0);//tempo de selecao

            int numClassesConhecidas = numClassesConhecidas(raizes);

            ioArff.saveArffFile(raizes, "raizes" + iteration);

            classify(classifiers, iteration, raizes, z3, folds, time, numClassesConhecidas, 0);

            iteration++;

            if ((z2.numInstances() - numInstancias) < numInstancias) {
                break;
            }

        } while (true);
    }

}
