/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package selecao;

import weka.clusterers.SimpleKMeans;
import weka.core.Instances;

/**
 *
 * @author guilherme
 */
public class Selecao implements ISelecao{

    @Override
    public Instances seleciona(SimpleKMeans clusterer, Instances raizes) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Instances getAmostrasSelecionadas() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
