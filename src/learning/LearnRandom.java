/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package learning;

import io.IOArff;
import io.IOText;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import raizes.Raizes;
import weka.core.Instances;

/**
 *
 * @author guilherme
 */
public class LearnRandom extends Learn {

    public void random(Instances z2, Instances z3,
            int xNumClasses, String classifiers) {

        isSupervisionado = tipoClassificador(classifiers);

        IOArff ioArff = new IOArff();

        int numInstancias = z2.attribute(z2.numAttributes() - 1).numValues() * xNumClasses;
        int iteration = 0;

        Raizes r = new Raizes();
        List<Instances> conjuntos = r.shuffle(z2, xNumClasses);
        Instances raizes = conjuntos.get(0);

        //atualiza z2 (amostras de z2 original menos amostras raizes de z1)
        z2 = conjuntos.get(1);

        Instances amostrasSelecionadasUnlabeled = new Instances(raizes);
        amostrasSelecionadasUnlabeled.delete();

        do {

            if (iteration != 0) {
                for (int i = 0; i < numInstancias; i++) {
                    raizes.add(z2.instance(i));
                }
                for (int i = 0; i < numInstancias; i++) {
                    z2.delete(i);
                }
            }
            
            classesConhecidas = new HashSet<>();
            int numClassesConhecidas = classesConhecidas(raizes);
            List<String> outClassesConhecidas = new ArrayList<>();
            outClassesConhecidas.add(String.valueOf(numClassesConhecidas));
            outClassesConhecidas.add(classesConhecidas.toString());
            new IOText().save(System.getProperty("user.dir").concat(File.separator),
                    "classesConhecidas", outClassesConhecidas);

            ioArff.saveArffFile(raizes, "raizes" + iteration);
            conjuntos = r.shuffle(z2, 2 * xNumClasses);
            Instances novasAmostras = conjuntos.get(0);
            
            z2 = conjuntos.get(1);
            if (isSupervisionado) {
                for (int i = 0; i < novasAmostras.numInstances(); i++) {
                    raizes.add(novasAmostras.instance(i));
                }
            } else {
                for (int i = 0; i < novasAmostras.numInstances(); i++) {
                    amostrasSelecionadasUnlabeled.add(novasAmostras.instance(i));
                }
                new IOArff().saveArffFile(amostrasSelecionadasUnlabeled, "unlabeled");
            }

            classifica(classifiers, raizes, z3, amostrasSelecionadasUnlabeled);

            //System.out.println(raizes.numInstances() + "+" + amostrasSelecionadasUnlabeled.numInstances() + "=" + (raizes.numInstances() + amostrasSelecionadasUnlabeled.numInstances()));

            salvaDados(classifiers, iteration);

            iteration++;

            if ((z2.numInstances() - numInstancias) < numInstancias) {
                break;
            }

        } while (true);
    }

}
