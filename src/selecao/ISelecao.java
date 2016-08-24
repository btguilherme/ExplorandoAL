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
public interface ISelecao {
    public Instances seleciona(SimpleKMeans clusterer, Instances raizes);
    public boolean isEmpty();
    public Instances getAmostrasSelecionadas();
}
