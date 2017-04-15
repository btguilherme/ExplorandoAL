/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.util.logging.Level;
import java.util.logging.Logger;
import selecao.RDS;
import weka.core.Instances;
import weka.filters.Filter;

/**
 *
 * @author guilherme
 */
public class RemoveAttClass {

    public static Instances removeAtributoClasse(Instances z2) {
        Instances dataClusterer = null;
        weka.filters.unsupervised.attribute.Remove filter = new weka.filters.unsupervised.attribute.Remove();
        filter.setAttributeIndices("" + (z2.classIndex() + 1));

        try {
            filter.setInputFormat(z2);
            dataClusterer = Filter.useFilter(z2, filter);
        } catch (Exception ex) {
            Logger.getLogger(RDS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dataClusterer;
    }

}
